package com.dantesting.omronsdktesting.communication

import android.util.Log
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants

class DeviceUtils {

    companion object {
        fun parseVitalData(vitalData: HashMap<String, Any>) {
            val bloodPressureItemList = vitalData[OmronConstants.OMRONVitalDataBloodPressureKey] as ArrayList<*>?
            if (bloodPressureItemList != null) {
                for (bpItem in bloodPressureItemList) {
                    Log.e("Blood Pressure - ", bpItem.toString())
                }
            }
        }

        fun getBloodPressureSettings(deviceSettings: java.util.ArrayList<java.util.HashMap<Any, Any>?>): java.util.ArrayList<java.util.HashMap<Any, Any>?> {

            val bloodPressurePersonalSettings = HashMap<String, Any>()
            bloodPressurePersonalSettings[OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadEnableKey] =
                OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadStatus.On
            bloodPressurePersonalSettings[OmronConstants.OMRONDevicePersonalSettings.BloodPressureTruReadIntervalKey] =
                OmronConstants.OMRONDevicePersonalSettingsBloodPressureTruReadInterval.Interval30
            val settings = HashMap<String, Any>()
            settings[OmronConstants.OMRONDevicePersonalSettings.BloodPressureKey] =
                bloodPressurePersonalSettings
            val personalSettings = HashMap<Any, Any>()
            personalSettings[OmronConstants.OMRONDevicePersonalSettingsKey] = settings

            deviceSettings.add(personalSettings)

            return deviceSettings
        }
    }
}