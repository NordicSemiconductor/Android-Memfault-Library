package com.nordicsemi.memfault.lib.data

import com.nordicsemi.memfault.lib.bluetooth.BluetoothLEStatus
import com.nordicsemi.memfault.lib.internet.UploadingStatus

data class MemfaultData(
    val bleStatus: BluetoothLEStatus = BluetoothLEStatus.IDLE,
    val uploadingStatus: UploadingStatus = UploadingStatus.SUSPENDED,
    val config: MemfaultConfig? = null,
    val chunks: List<Chunk> = emptyList()
) {

    val pendingChunksSize: Int

    init {
        pendingChunksSize = chunks.filter { !it.isUploaded }.size
    }
}

data class Chunk(
    val chunkNumber: Int,
    val data: ByteArray,
    val deviceId: String,
    val isUploaded: Boolean
)
