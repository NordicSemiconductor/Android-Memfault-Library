/*
 * Copyright (c) 2022, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 * OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.memfault.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import no.nordicsemi.memfault.lib.data.Chunk
import no.nordicsemi.memfault.lib.data.MemfaultConfig
import no.nordicsemi.memfault.lib.data.toChunk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.ktx.suspendForValidResponse
import java.util.*

val MDS_SERVICE_UUID: UUID = UUID.fromString("54220000-f6a5-4007-a371-722f4ebd8436")
private val MDS_SUPPORTED_FEATURES_CHARACTERISTIC_UUID =
    UUID.fromString("54220001-f6a5-4007-a371-722f4ebd8436")
private val MDS_DEVICE_ID_CHARACTERISTIC_UUID =
    UUID.fromString("54220002-f6a5-4007-a371-722f4ebd8436")
private val MDS_DATA_URI_CHARACTERISTIC_UUID =
    UUID.fromString("54220003-f6a5-4007-a371-722f4ebd8436")
private val MDS_AUTHORISATION_CHARACTERISTIC_UUID =
    UUID.fromString("54220004-f6a5-4007-a371-722f4ebd8436")
private val MDS_DATA_EXPORT_CHARACTERISTIC_UUID =
    UUID.fromString("54220005-f6a5-4007-a371-722f4ebd8436")

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

    private val dataHolder = ConnectionObserverAdapter()
    val status = dataHolder.status

    private val _config = MutableStateFlow<MemfaultConfig?>(null)
    val config = _config.asStateFlow()

    private val _receivedChunk = MutableSharedFlow<Chunk>(extraBufferCapacity = 10, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val receivedChunk = _receivedChunk.asSharedFlow()

    init {
        connectionObserver = dataHolder
    }

    override fun log(priority: Int, message: String) {
        Log.d(LOG, message)
    }

    override fun getMinLogPriority(): Int {
        return Log.VERBOSE
    }

    override fun getGattCallback(): BleManagerGattCallback {
        return MemfaultManagerGattCallback()
    }

    private inner class MemfaultManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            val handler = CoroutineExceptionHandler { _, it ->
                it.printStackTrace()
                dataHolder.onError()
            }

            scope.launch(handler) {
                val deviceId = readCharacteristic(mdsDeviceIdCharacteristic)
                    .suspendForValidResponse<StringReadResponse>()
                    .value!!
                val url = readCharacteristic(mdsDataUriCharacteristic)
                    .suspendForValidResponse<StringReadResponse>()
                    .value!!
                val authorisation = readCharacteristic(mdsAuthorisationCharacteristic)
                    .suspendForValidResponse<StringReadResponse>()
                    .value!!

                val authorisationHeader = AuthorisationHeader(authorisation)
                val config = MemfaultConfig(authorisationHeader, url, deviceId)
                _config.value = config

                launch {
                    setNotificationCallback(mdsDataExportCharacteristic).asValidResponseFlow<ByteReadResponse>()
                        .cancellable()
                        .map { it.value }
                        .collect { it?.let { _receivedChunk.tryEmit(it.toChunk(deviceId)) } }
                }

                enableNotifications(mdsDataExportCharacteristic).suspend()

                writeCharacteristic(
                    mdsDataExportCharacteristic,
                    byteArrayOf(0x01),
                    BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                ).suspend()
            }
            requestMtu(512).enqueue()
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

    suspend fun disconnectWithCatch() {
        try {
            disconnect().suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun start(device: BluetoothDevice) {
        try {
            connect(device)
                .useAutoConnect(false)
                .retry(3, 100)
                .suspend()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
