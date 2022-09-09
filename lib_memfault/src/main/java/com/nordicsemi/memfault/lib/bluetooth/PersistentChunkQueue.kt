package com.nordicsemi.memfault.lib.bluetooth

import com.memfault.cloud.sdk.ChunkQueue
import com.squareup.tape2.QueueFile
import java.io.File
import java.io.IOException

/**
 * A sample persistent queue implementation.
 */
class PersistentChunkQueue(
    file: File
) : ChunkQueue {
    private val queueFile = QueueFile.Builder(file).build()

    /**
     * Add chunks to the queue.
     *
     * If there is an error adding a chunk to the queue, the entire operation will stop at that chunk.
     * Any previous chunks that were added will remain enqueued and not be rolled back.
     *
     * @throws IOException if there was an error adding chunks to the queue.
     */
    @Synchronized
    override fun addChunks(chunks: List<ByteArray>): Boolean {
        for ((index, chunk) in chunks.withIndex()) {
            try {
                queueFile.add(chunk)
            } catch (e: IOException) {
                throw IOException("Error adding chunk at index $index", e)
            }
        }
        return true
    }

    /**
     * Remove the specified number of chunks from the head of the queue.
     *
     * @throws IOException if there was an error removing chunks from the queue.
     */
    @Synchronized
    override fun drop(count: Int) {
        check(count >= 0)
        queueFile.remove(count)
    }

    /**
     * Return at most `count` items from the head of the queue.
     */
    @Synchronized
    override fun peek(count: Int): List<ByteArray> {
        check(count >= 0)
        val result = mutableListOf<ByteArray>()
        for ((index, bytes) in queueFile.withIndex()) {
            if (index > count) break
            result.add(bytes)
        }
        return result.toList()
    }
}
