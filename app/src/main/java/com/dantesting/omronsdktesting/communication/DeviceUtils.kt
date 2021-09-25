package com.dantesting.omronsdktesting.communication

import android.util.Log
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants

class DeviceUtils {

    companion object {
        fun parseVitalData(vitalData: HashMap<String, Any>) {

            // Blood Pressure Data
            val bloodPressureItemList = vitalData[OmronConstants.OMRONVitalDataBloodPressureKey] as ArrayList<*>?
            if (bloodPressureItemList != null) {
                for (bpItem in bloodPressureItemList) {
                    Log.e("Blood Pressure - ", bpItem.toString())
                }
            }

            // Activity Data
            val activityList =
                vitalData[OmronConstants.OMRONVitalDataActivityKey] as java.util.ArrayList<java.util.HashMap<String, Any>>?
            if (activityList != null) {
                for (activityItem in activityList) {
                    val list: List<String> = java.util.ArrayList(activityItem.keys)
                    for (key in list) {
                        Log.e("Activity key - ", key)
                        Log.e("Activity Data - ", activityItem[key].toString())
                    }
                }
            }

            // Sleep Data
            val sleepingData =
                vitalData[OmronConstants.OMRONVitalDataSleepKey] as java.util.ArrayList<java.util.HashMap<String, Any>>?
            if (sleepingData != null) {
                for (sleepitem in sleepingData) {
                    Log.e("Sleep - ", sleepitem.toString())
                }
            }

            // Records Data
            val recordData =
                vitalData[OmronConstants.OMRONVitalDataRecordKey] as java.util.ArrayList<java.util.HashMap<String, Any>>?
            if (recordData != null) {
                for (recordItem in recordData) {
                    Log.e("Record - ", recordItem.toString())
                }
            }

            // Weight Data
            val weightData =
                vitalData[OmronConstants.OMRONVitalDataWeightKey] as java.util.ArrayList<java.util.HashMap<String, Any>>?
            if (weightData != null) {
                for (weightItem in weightData) {
                    Log.e("Weight - ", weightItem.toString())
                }
            }

            // Pulse oxximeter Data
            val pulseOximeterData =
                vitalData[OmronConstants.OMRONVitalDataPulseOximeterKey] as java.util.ArrayList<java.util.HashMap<String, Any>>?
            if (pulseOximeterData != null) {
                for (pulseOximeterItem in pulseOximeterData) {
                    Log.e("Pulse Oximeter - ", pulseOximeterItem.toString())
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