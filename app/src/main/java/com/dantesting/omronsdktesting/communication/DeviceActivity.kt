package com.dantesting.omronsdktesting.communication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.dantesting.omronsdktesting.R
import com.dantesting.omronsdktesting.communication.DeviceUtils.Companion.getBloodPressureSettings
import com.dantesting.omronsdktesting.communication.DeviceUtils.Companion.parseVitalData
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.DeviceConfiguration.OmronPeripheralManagerConfig
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.SharedManager
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronErrorInfo
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants
import kotlin.collections.HashMap


class DeviceActivity : AppCompatActivity() {

    companion object {
        val MAC_ADDRESS = "MAC_ADDRESS"
        val NAME = "NAME"
        val IS_BONDED = "IS_BONDED"
    }

    private val TAG: String = this::class.simpleName as String

    private lateinit var mac: String

    private lateinit var omronSdk: SharedManager

    private var device: OmronPeripheral? = null

    private lateinit var btnConnect: Button
    private lateinit var btnData: Button
    private lateinit var loading: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device)

        if (intent.getStringExtra(NAME) != null && intent.getStringExtra(MAC_ADDRESS) != null) {
            title = intent.getStringExtra(NAME)
            mac = intent.getStringExtra(MAC_ADDRESS)!!
            if (intent.getBooleanExtra(IS_BONDED, false)) {
                device = OmronPeripheral(title.toString(), mac)
            }
        }
        else {
            throw Exception("intent without mac or name")
        }

        btnConnect = findViewById(R.id.connect)
        btnData = findViewById(R.id.data)
        loading = findViewById(R.id.progressBar2)

        btnConnect.setOnClickListener {
            startScan()
        }

        btnData.setOnClickListener {
            receiveData()
        }

        omronSdk = OmronPeripheralManager.sharedManager(applicationContext)

        omronSdk.setAPIKey("4465A916-A1DF-4162-A6FE-71AC9525EB99", null)

        init_omron_sdk()
    }

    private fun init_omron_sdk() {

        // Запускаем инициализацию SDK

        showLoad()

        if (device != null && device?.deviceInformation?.get(OmronConstants.OMRONBLEConfigDevice.GroupID) != null && device?.deviceInformation?.get(
                OmronConstants.OMRONBLEConfigDevice.GroupIncludedGroupID
            ) != null
        ) {

            val filterDevices: MutableList<java.util.HashMap<String, String>> =
                java.util.ArrayList()
            filterDevices.add(device!!.deviceInformation)
            OmronPeripheralManagerConfig.deviceFilters = filterDevices
        }

        var deviceSettings = java.util.ArrayList<java.util.HashMap<Any, Any>?>()

        deviceSettings = getBloodPressureSettings(deviceSettings)

        OmronPeripheralManagerConfig.deviceSettings = deviceSettings
        OmronPeripheralManagerConfig.timeoutInterval = 30
        OmronPeripheralManagerConfig.userHashId =
            "<email_address_of_user>"

        if (device?.deviceInformation?.get(OmronConstants.OMRONBLEConfigDevice.Category)
                ?.toInt() != OmronConstants.OMRONBLEDeviceCategory.ACTIVITY
        ) {
            // Reads all data from device.
            OmronPeripheralManagerConfig.enableAllDataRead = true
        }

        OmronPeripheralManager.sharedManager(applicationContext)
            .startManager()

        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver,
            IntentFilter(OmronConstants.OMRONBLEConfigDeviceAvailabilityNotification)
        )

        omronSdk.startManager()
    }

    private val mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            // SDK инициализировалось, запускаем сканирование (требуется только при первом запуске SDK)

            val status: Int =
                intent.getIntExtra(OmronConstants.OMRONConfigurationStatusKey, 0);
            if (status ==
                OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileSuccess) {
                Log.d(TAG, "Config File Extract Success")

                hideLoad()

            }else  {
                Log.d(TAG, "Config File Extract Failure")
            }
        }
    }

    private fun startScan() {

        // настраиваем и запускаем сканирование

        showLoad()

        omronSdk.startScanPeripherals { arrayList, omronErrorInfo ->
            if (omronErrorInfo.resultCode == 0) {
                if (arrayList[0].uuid == mac) {
                    device = arrayList[0]
                    stopScan()
                    connect()
                }
            }
            else {
                Log.e(TAG, omronErrorInfo.resultCode.toString() + " " + omronErrorInfo.messageInfo)
                hideLoad()
            }
        }
    }

    private fun stopScan() {
        omronSdk.stopScanPeripherals {
            if (it.resultCode != 0) {
                Log.e(TAG, "Error Code : " + it.resultCode + "\nError Detail Code : " + it.messageInfo)
                makeToast("Ошибка во время сканирования")
            }
        }
    }

    private fun connect() {
        omronSdk.connectPeripheral(device) { device, resultInfo ->
            if (resultInfo.resultCode == 0) {
                Log.e(TAG, "Connected")
                makeToast("Сопряжение прошло успешно!")
            } else {
                Log.e(TAG, "Error Code : " + resultInfo.resultCode + "\nError Detail : " + resultInfo.messageInfo)
                makeToast("Ошибка во время сопряжения!")
            }
            hideLoad()
        }
    }

    private fun receiveData() {

        showLoad()

        if (device == null) {
            makeToast("Перед получением данных нужно подключиться к устройству...")
            startScan()
        }
        else {
            init_omron_sdk()
            omronSdk.startDataTransferFromPeripheral(OmronPeripheral(device!!.localName, device!!.uuid), 1, true, OmronConstants.OMRONVitalDataTransferCategory.BloodPressure) { peripheral, resultInfo ->
                if (resultInfo.resultCode == 0) {

                    val output = peripheral.vitalData

                    if (output is OmronErrorInfo) {
                        Log.e(TAG, "Error Code : " + output.resultCode + "\nError Detail : " + output.messageInfo)
                    }
                    else {
                        Log.e(TAG, "Data received! ${output as HashMap<String, Any>}")

                        makeToast("Данные получены!")

                        parseVitalData(output)

                        endReceive()
                    }

                } else {
                    Log.e(TAG, "Error Code : " + resultInfo.resultCode + "\nError Detail : " + resultInfo.messageInfo)
                    makeToast("Ошибка во время получения данных!")
                }
                hideLoad()
            }
        }
    }

    private fun endReceive() {
        omronSdk.endDataTransferFromPeripheral { omronPeripheral, resultInfo ->
            if (resultInfo.resultCode != 0) {
                Log.e(TAG, "Error Code : " + resultInfo.resultCode + "\nError Detail : " + resultInfo.messageInfo)
                makeToast("При окончании передачи данных произошла ошибка!")
            }
        }
    }

    private fun showLoad() {
        runOnUiThread {
            loading.visibility = View.VISIBLE
            btnConnect.visibility = View.INVISIBLE
            btnData.visibility = View.INVISIBLE
        }
    }

    private fun hideLoad() {
        runOnUiThread {
            loading.visibility = View.INVISIBLE
            btnConnect.visibility = View.VISIBLE
            btnData.visibility = View.VISIBLE
        }
    }

    private fun makeToast(msg: String) {
        runOnUiThread {
            Toast.makeText(
                applicationContext,
                msg,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}