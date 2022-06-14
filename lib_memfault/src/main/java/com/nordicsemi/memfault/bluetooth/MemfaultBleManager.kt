package com.nordicsemi.memfault.bluetooth

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
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

    private val LOG = "MEMFAULT"

    private var mdsSupportedFeaturesCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDeviceIdCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDataUriCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsAuthorisationCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDataExportCharacteristic: BluetoothGattCharacteristic? = null

    val dataHolder = ConnectionObserverAdapter<MemfaultEntity>()

    init {
        connectionObserver = dataHolder
        dataHolder.setValue(MemfaultDataNotAvailableEntity)
    }

    override fun log(priority: Int, message: String) {
        Log.d(LOG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
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
                    .suspendForValidResponse<StringReadResponse>()
                    .value!!
                val url = readCharacteristic(mdsDataUriCharacteristic)
                    .suspendForValidResponse<StringReadResponse>()
                    .value!!
                val authorisation = readCharacteristic(mdsAuthorisationCharacteristic)
                    .suspendForValidResponse<StringReadResponse>()
                    .value!!

                val config = ConfigData(AuthorisationHeader(authorisation), deviceId, url)

                setNotificationCallback(mdsDataExportCharacteristic).asValidResponseFlow<StringReadResponse>().onEach {
                    dataHolder.setValue(MemfaultDataEntity(config, it.value!!))
                }.launchIn(scope)
                enableNotifications(mdsDataExportCharacteristic).enqueue()
            }
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
