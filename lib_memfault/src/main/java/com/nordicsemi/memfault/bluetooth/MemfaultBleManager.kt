package com.nordicsemi.memfault.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.nordicsemi.memfault.bluetooth.responses.AuthorisationResponse
import com.nordicsemi.memfault.bluetooth.responses.DataUriResponse
import com.nordicsemi.memfault.bluetooth.responses.DeviceIdReadResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.common.callback.battery.BatteryLevelResponse
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspendForValidResponse
import java.util.*

val MDS_SERVICE_UUID: UUID = UUID.fromString("54220000-f6a5-4007-a371-722f4ebd8436")
private val MDS_SUPPORTED_FEATURES_CHARACTERISTIC_UUID = UUID.fromString("54220001-f6a5-4007-a371-722f4ebd8436")
private val MDS_DEVICE_ID_CHARACTERISTIC_UUID = UUID.fromString("54220002-f6a5-4007-a371-722f4ebd8436")
private val MDS_DATA_URI_CHARACTERISTIC_UUID = UUID.fromString("54220003-f6a5-4007-a371-722f4ebd8436")
private val MDS_AUTHORISATION_CHARACTERISTIC_UUID = UUID.fromString("54220004-f6a5-4007-a371-722f4ebd8436")
private val MDS_DATA_EXPORT_CHARACTERISTIC_UUID = UUID.fromString("54220005-f6a5-4007-a371-722f4ebd8436")

internal class MemfaultBleManager(
    context: Context,
    private val scope: CoroutineScope
) : BleManager(context) {

    private var mdsSupportedFeaturesCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDeviceIdCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDataUriCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsAuthorisationCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDataExportCharacteristic: BluetoothGattCharacteristic? = null

    private val data = MutableStateFlow(CSCData())
    val dataHolder = ConnectionObserverAdapter<CSCData>()

    init {
        connectionObserver = dataHolder

        data.onEach {
            dataHolder.setValue(it)
        }.launchIn(scope)
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return CSCManagerGattCallback()
    }

    private inner class CSCManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            scope.launch {
                //TODO error handling
                val deviceId = readCharacteristic(mdsDeviceIdCharacteristic)
                    .suspendForValidResponse<DeviceIdReadResponse>()
                    .authorisation!!
                val uri = readCharacteristic(mdsDataUriCharacteristic)
                    .suspendForValidResponse<DataUriResponse>()
                    .uri!!
                val authorisation = readCharacteristic(mdsAuthorisationCharacteristic)
                    .suspendForValidResponse<AuthorisationResponse>()
                    .authorisation!!

                val config = ConfigData(deviceId, uri, authorisation)
            }

            setNotificationCallback(mdsDataExportCharacteristic).asValidResponseFlow<BatteryLevelResponse>().onEach {
                data.value = data.value.copy(batteryLevel = it.batteryLevel)
            }.launchIn(scope)
            enableNotifications(mdsDataExportCharacteristic).enqueue()
        }

        public override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            gatt.getService(MDS_SERVICE_UUID)?.run {
                mdsSupportedFeaturesCharacteristic = getCharacteristic(MDS_SUPPORTED_FEATURES_CHARACTERISTIC_UUID)
                mdsDeviceIdCharacteristic = getCharacteristic(MDS_DEVICE_ID_CHARACTERISTIC_UUID)
                mdsDataUriCharacteristic = getCharacteristic(MDS_DATA_URI_CHARACTERISTIC_UUID)
                mdsAuthorisationCharacteristic = getCharacteristic(MDS_AUTHORISATION_CHARACTERISTIC_UUID)
                mdsDataExportCharacteristic = getCharacteristic(MDS_DATA_EXPORT_CHARACTERISTIC_UUID)

            }
            return mdsSupportedFeaturesCharacteristic != null
                    && mdsDeviceIdCharacteristic != null
                    && mdsDataUriCharacteristic != null
                    && mdsAuthorisationCharacteristic != null
                    && mdsDataExportCharacteristic != null
        }

        override fun onServicesInvalidated() {
            mdsSupportedFeaturesCharacteristic = null
            mdsDeviceIdCharacteristic = null
            mdsDataUriCharacteristic = null
            mdsAuthorisationCharacteristic = null
            mdsDataExportCharacteristic = null
        }
    }
}
