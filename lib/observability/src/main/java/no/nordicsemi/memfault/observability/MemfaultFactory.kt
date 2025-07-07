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

package no.nordicsemi.memfault.observability

import android.content.Context
import androidx.room.Room
import com.memfault.cloud.sdk.ChunkQueue
import no.nordicsemi.memfault.common.internet.InternetStateManager
import no.nordicsemi.memfault.observability.bluetooth.ChunksBleManager
import no.nordicsemi.memfault.observability.data.MemfaultConfig
import no.nordicsemi.memfault.observability.db.ChunksDatabase
import no.nordicsemi.memfault.observability.internet.ChunkUploadManager
import no.nordicsemi.memfault.observability.internet.DBChunkQueue

private val DB_NAME = "chunks-database"

internal class MemfaultFactory(private val context: Context) {
    private var database: ChunksDatabase? = null

    fun getScope() = MemfaultScope

    fun getDatabase(): ChunksDatabase {
        return database ?: Room.databaseBuilder(context, ChunksDatabase::class.java, DB_NAME)
            .build()
            .also { database = it }
    }

    private fun getChunkQueue(deviceId: String): ChunkQueue {
        return DBChunkQueue(deviceId, getDatabase().chunksDao())
    }

    fun getUploadManager(config: MemfaultConfig): ChunkUploadManager {
        return ChunkUploadManager(config, getChunkQueue(config.deviceId))
    }

    fun getMemfaultManager(): ChunksBleManager {
        return ChunksBleManager(context, getScope())
    }

    fun getInternetStateManager(context: Context): InternetStateManager {
        return InternetStateManager(context)
    }
}
