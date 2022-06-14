package com.nordicsemi.memfault.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.nordicsemi.memfault.bluetooth.AuthorisationHeader
import com.nordicsemi.memfault.bluetooth.MemfaultBleManager
import com.nordicsemi.memfault.bluetooth.MemfaultDataEntity
import com.nordicsemi.memfault.bluetooth.SuccessResult
import com.nordicsemi.memfault.network.NetworkApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MemfaultManager {

    companion object {

        fun install(context: Context, device: BluetoothDevice) {
            val bleManager = MemfaultBleManager(context, GlobalScope)
            bleManager.connect(device)

            bleManager.dataHolder.status.onEach {
                ((it as? SuccessResult)?.data as? MemfaultDataEntity)?.let {
                    val network = createNetwork(it.config.authorisation)
                    network.sendLog(it.config.url, it.message)
                }
            }.launchIn(GlobalScope)
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
            }.build()

            val retrofit = Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create())
                .client(httpClient)
                .build()

            return retrofit.create(NetworkApi::class.java)
        }
    }
}
