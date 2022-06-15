package com.nordicsemi.memfault.repository

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.nordicsemi.memfault.bluetooth.*
import com.nordicsemi.memfault.network.NetworkApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Inject

class MemfaultManager @Inject constructor() {

    private var manager: MemfaultBleManager? = null
    val status: StateFlow<BleManagerResult<MemfaultEntity>>?
        get() = manager?.dataHolder?.status

    suspend fun install(context: Context, device: BluetoothDevice) {
        Log.d("AAATESTAAA", "install()")
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
        }.build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(MoshiConverterFactory.create())
            .client(httpClient)
            .build()

        return retrofit.create(NetworkApi::class.java)
    }
}
