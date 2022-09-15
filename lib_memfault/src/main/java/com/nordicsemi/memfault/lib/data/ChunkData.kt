package com.nordicsemi.memfault.lib.data

fun ByteArray.toChunk(deviceId: String): Chunk {
    val chunkNumber = this[0].toInt()

    val data = this.copyOfRange(1, this.size)
    return Chunk(chunkNumber, data, deviceId, false)
}
