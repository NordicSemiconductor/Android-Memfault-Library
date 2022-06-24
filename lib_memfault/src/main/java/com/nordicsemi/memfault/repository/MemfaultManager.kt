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

package com.nordicsemi.memfault.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.nordicsemi.memfault.bluetooth.*
import com.nordicsemi.memfault.network.NetworkApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class MemfaultManager @Inject constructor() {

    private var manager: MemfaultBleManager? = null
    val status: StateFlow<BleManagerResult<MemfaultEntity>>?
        get() = manager?.dataHolder?.status

    suspend fun install(context: Context, device: BluetoothDevice) {
        val bleManager = MemfaultBleManager(context, GlobalScope)
        manager = bleManager
        bleManager.dataHolder.status.onEach {
            ((it as? SuccessResult)?.data as? MemfaultDataEntity)?.let {
                val network = createNetwork(it.config.authorisation)
                network.sendLog(it.config.url, it.message)
            }
        }.launchIn(GlobalScope)

        bleManager.start(device)
    }

    private fun createNetwork(header: AuthorisationHeader): NetworkApi {
        val httpClient = OkHttpClient.Builder().apply {
            addInterceptor { chain ->
                val request = chain.request()
                    .newBuilder()
                    .addHeader(header.key, header.value)
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
