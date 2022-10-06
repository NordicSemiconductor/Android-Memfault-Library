package no.nordicsemi.memfault.lib.internet

import com.memfault.cloud.sdk.ChunkQueue
import com.memfault.cloud.sdk.ChunkSender
import com.memfault.cloud.sdk.MemfaultCloud
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.memfault.lib.data.MemfaultConfig

internal class ChunkUploadManager(
    config: MemfaultConfig,
    chunkQueue: ChunkQueue
) {
    private val _status = MutableStateFlow<UploadingStatus>(UploadingStatus.Offline)
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

    /**
     * Uploads already collected chunks to the cloud.
     *
     * It should be triggered in 3 scenarios:
     * 1. After successful connection the already stored chunks in DB should be uploaded.
     * 2. After received chunks.
     * 3. After suspended delay got from previous upload.
     */
    suspend fun uploadChunks() = coroutineScope {
        if (status.value is UploadingStatus.Suspended) {
            return@coroutineScope
        }
        _status.value = UploadingStatus.InProgress

        val result = memfaultSender.send()

        (result as? ChunkUploadError)?.let {
            launchSuspendTask(it.delay)
        }
    }

    private suspend fun launchSuspendTask(delay: Long) {
        for (i in delay downTo 0) {
            _status.value = UploadingStatus.Suspended(i)
            delay(1000)
        }

        _status.value = UploadingStatus.InProgress
        uploadChunks()
    }

    fun deinit() {
        memfaultCloud.deinit()
        _status.value = UploadingStatus.Offline
    }
}
