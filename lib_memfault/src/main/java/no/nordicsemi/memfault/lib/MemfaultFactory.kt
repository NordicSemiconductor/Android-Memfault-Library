package no.nordicsemi.memfault.lib

import android.content.Context
import androidx.room.Room
import com.memfault.cloud.sdk.ChunkQueue
import no.nordicsemi.memfault.lib.bluetooth.ChunkValidator
import no.nordicsemi.memfault.lib.bluetooth.ChunksBleManager
import no.nordicsemi.memfault.lib.data.MemfaultConfig
import no.nordicsemi.memfault.lib.db.ChunksDatabase
import no.nordicsemi.memfault.lib.internet.ChunkUploadManager
import no.nordicsemi.memfault.lib.internet.DBChunkQueue
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

    private fun getChunkQueue(deviceId: String): ChunkQueue {
        return DBChunkQueue(deviceId, getDatabase().chunksDao())
    }

    fun getUploadManager(config: MemfaultConfig): ChunkUploadManager {
        return ChunkUploadManager(config, getChunkQueue(config.deviceId))
    }

    fun getMemfaultManager(): ChunksBleManager {
        return ChunksBleManager(context, getScope())
    }

    fun getChunkValidator(): ChunkValidator {
        return ChunkValidator()
    }

    fun getInternetStateManager(context: Context): InternetStateManager {
        return InternetStateManager(context)
    }
}
