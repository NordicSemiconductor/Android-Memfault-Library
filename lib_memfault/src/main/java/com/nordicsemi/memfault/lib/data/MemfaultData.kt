package com.nordicsemi.memfault.lib.data

import com.nordicsemi.memfault.lib.bluetooth.BluetoothLEStatus
import com.nordicsemi.memfault.lib.internet.UploadingStatus

data class MemfaultData(
    val bleStatus: BluetoothLEStatus = BluetoothLEStatus.IDLE,
    val uploadingStatus: UploadingStatus = UploadingStatus.UPLOADING,
    val config: MemfaultConfig? = null,
    val chunks: List<Chunk> = emptyList()
)

data class Chunk(
    val number: Int,
    val data: ByteArray,
    val isUploaded: Boolean
)
