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

package no.nordicsemi.memfault.observability.internet

import com.memfault.cloud.sdk.ChunkQueue
import com.memfault.cloud.sdk.ChunkSender
import com.memfault.cloud.sdk.MemfaultCloud
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.memfault.observability.data.MemfaultConfig

internal class ChunkUploadManager(
    config: MemfaultConfig,
    chunkQueue: ChunkQueue
) {
    private val _status = MutableStateFlow<UploadingStatus>(UploadingStatus.Offline)
    val status = _status.asStateFlow()

    private val memfaultCloud: MemfaultCloud = MemfaultCloud.Builder()
        .setApiKey(config.authorisationHeader.value)
        .build()
    private val memfaultSender: ChunkSender = ChunkSender.Builder()
        .setMemfaultCloud(memfaultCloud)
        .setChunkQueue(chunkQueue)
        .setDeviceSerialNumber(config.deviceId)
        .build()

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
