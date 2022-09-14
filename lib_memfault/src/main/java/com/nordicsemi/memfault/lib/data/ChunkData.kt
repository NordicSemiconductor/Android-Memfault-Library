package com.nordicsemi.memfault.lib.data

import android.util.Log

fun ByteArray.toChunk(): Chunk {
    val chunkNumber = this[0].toInt()

    val data = this.copyOfRange(1, this.size)
    return Chunk(chunkNumber, data, false)
}
