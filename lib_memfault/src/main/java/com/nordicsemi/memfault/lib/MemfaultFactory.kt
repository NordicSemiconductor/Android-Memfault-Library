package com.nordicsemi.memfault.lib

import android.content.Context
import androidx.room.Room
import com.memfault.cloud.sdk.ChunkQueue
import com.nordicsemi.memfault.lib.bluetooth.ChunkValidator
import com.nordicsemi.memfault.lib.bluetooth.MemfaultBleManager
import com.nordicsemi.memfault.lib.data.MemfaultConfig
import com.nordicsemi.memfault.lib.db.ChunksDatabase
import com.nordicsemi.memfault.lib.internet.ChunkUploadManager
import com.nordicsemi.memfault.lib.internet.DBChunkQueue
import no.nordicsemi.android.common.permission.internet.InternetStateManager

private val DB_NAME = "chunks-database"

internal class MemfaultFactory(private val context: Context) {

    private var database: ChunksDatabase? = null

    fun getScope() = MemfaultScope

    fun getDatabase(): ChunksDatabase {
        return database ?: Room.databaseBuilder(context, ChunksDatabase::class.java, DB_NAME)
            .build()
            .also { database = it }
    }

    fun getChunkQueue(): ChunkQueue {
        return DBChunkQueue(getDatabase().chunksDao())
    }

    fun getUploadManager(config: MemfaultConfig): ChunkUploadManager {
        return ChunkUploadManager(config, getChunkQueue())
    }

    fun getMemfaultManager(): MemfaultBleManager {
        return MemfaultBleManager(context, getScope())
    }

    fun getChunkValidator(): ChunkValidator {
        return ChunkValidator()
    }

    fun getInternetStateManager(context: Context): InternetStateManager {
        return InternetStateManager(context)
    }
}
