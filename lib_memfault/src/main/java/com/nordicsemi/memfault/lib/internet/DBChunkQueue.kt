package com.nordicsemi.memfault.lib.internet

import com.memfault.cloud.sdk.ChunkQueue
import com.nordicsemi.memfault.lib.db.ChunksDao

internal class DBChunkQueue(
    private val chunkDao: ChunksDao
) : ChunkQueue {

    @Deprecated("This function can't be used, because of a limited signature for a parameter.")
    override fun addChunks(chunks: List<ByteArray>): Boolean {
        throw IllegalAccessException()
    }

    override fun drop(count: Int) {
        chunkDao.drop(count)
    }

    override fun peek(count: Int): List<ByteArray> {
        return chunkDao.getNotUploaded(count).map { it.data }
    }
}