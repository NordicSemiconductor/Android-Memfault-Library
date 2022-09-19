package no.nordicsemi.memfault.lib.internet

import com.memfault.cloud.sdk.ChunkQueue
import no.nordicsemi.memfault.lib.db.ChunksDao

internal class DBChunkQueue(
    private val deviceId: String,
    private val chunkDao: ChunksDao
) : ChunkQueue {

    @Deprecated("This function can't be used, because of a limited signature for a parameter.")
    override fun addChunks(chunks: List<ByteArray>): Boolean {
        throw IllegalAccessException()
    }

    override fun drop(count: Int) {
        chunkDao.drop(count, deviceId)
    }

    override fun peek(count: Int): List<ByteArray> {
        return chunkDao.getNotUploaded(count, deviceId).map { it.data }
    }
}