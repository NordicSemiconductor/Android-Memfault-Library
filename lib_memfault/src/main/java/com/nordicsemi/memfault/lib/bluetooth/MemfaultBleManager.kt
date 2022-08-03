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

package com.nordicsemi.memfault.lib.bluetooth

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.util.Log
import com.nordicsemi.memfault.lib.network.NetworkApi
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.ktx.asValidResponseFlow
import no.nordicsemi.android.ble.ktx.suspend
import no.nordicsemi.android.ble.ktx.suspendForValidResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
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

    private val chunkValidator = ChunkValidator()
    private var mdsSupportedFeaturesCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDeviceIdCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDataUriCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsAuthorisationCharacteristic: BluetoothGattCharacteristic? = null
    private var mdsDataExportCharacteristic: BluetoothGattCharacteristic? = null

    val dataHolder = ConnectionObserverAdapter<MemfaultEntity>()

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
        return CSCManagerGattCallback()
    }

    private inner class CSCManagerGattCallback : BleManagerGattCallback() {
        override fun initialize() {
            super.initialize()

            val handler = CoroutineExceptionHandler { _, exception ->
                dataHolder.updateError(exception)
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

                val config = ConfigData(AuthorisationHeader(authorisation, 0), deviceId, url)

                launch {
                    setNotificationCallback(mdsDataExportCharacteristic).asValidResponseFlow<ByteReadResponse>()
                        .cancellable()
                        .collect {
                            val chunkNumber = it.chunkNumber!!.toInt()
                            val data = it.value!!
                            val network =
                                createNetwork(AuthorisationHeader(authorisation, chunkNumber))

                            chunkValidator.validateChunk(chunkNumber)

                            val request = ByteArrayRequestBody(data)
                            network.sendLog(config.url, request)

                            dataHolder.updateProgress(chunkNumber, data)
                        }
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
                mdsSupportedFeaturesCharacteristic =
                    getCharacteristic(MDS_SUPPORTED_FEATURES_CHARACTERISTIC_UUID)
                mdsDeviceIdCharacteristic = getCharacteristic(MDS_DEVICE_ID_CHARACTERISTIC_UUID)
                mdsDataUriCharacteristic = getCharacteristic(MDS_DATA_URI_CHARACTERISTIC_UUID)
                mdsAuthorisationCharacteristic =
                    getCharacteristic(MDS_AUTHORISATION_CHARACTERISTIC_UUID)
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

    fun disconnectWithCatch() {
        try {
            disconnect().enqueue()
        } catch (e: Exception) {
            //do nothing
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

    private fun createNetwork(header: AuthorisationHeader): NetworkApi {
        val httpClient = OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .addHeader(header.key, header.value)
                    .addHeader("Chunk", "${header.chunkNumber}")
                    .build()

                chain.proceed(request)
            }
            addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
        }.build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://nordicsemi.com")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(httpClient)
            .build()

        return retrofit.create(NetworkApi::class.java)
    }
}
