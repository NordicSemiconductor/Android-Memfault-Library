package com.nordicsemi.memfault.lib.internet

import com.memfault.cloud.sdk.ChunkQueue
import com.memfault.cloud.sdk.ChunkSender
import com.memfault.cloud.sdk.MemfaultCloud
import com.nordicsemi.memfault.lib.data.MemfaultConfig
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class ChunkUploadManager(
    config: MemfaultConfig,
    chunkQueue: ChunkQueue
) {
    private val _status = MutableStateFlow(UploadingStatus.UPLOADING)
    val status = _status.asStateFlow()

    private val memfaultCloud: MemfaultCloud
    private val memfaultSender: ChunkSender

    init {
        memfaultCloud = MemfaultCloud.Builder()
            .setApiKey(config.authorisationHeader.value)
            .build()

        memfaultSender = ChunkSender.Builder()
            .setMemfaultCloud(memfaultCloud)
            .setChunkQueue(chunkQueue)
            .setDeviceSerialNumber(config.deviceId)
            .build()
    }

    suspend fun uploadChunks() = coroutineScope {
        if (status.value == UploadingStatus.SUSPENDED) {
            return@coroutineScope
        }

        val result = memfaultSender.send()

        (result as? ChunkUploadError)?.let {
            _status.value = UploadingStatus.SUSPENDED
            delay(it.delay)
            _status.value = UploadingStatus.UPLOADING
        }
    }

    fun deinit() {
        memfaultCloud.deinit()
    }
}
