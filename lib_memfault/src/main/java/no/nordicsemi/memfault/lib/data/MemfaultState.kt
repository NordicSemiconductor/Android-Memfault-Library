package no.nordicsemi.memfault.lib.data

import no.nordicsemi.memfault.lib.bluetooth.BluetoothLEStatus
import no.nordicsemi.memfault.lib.internet.UploadingStatus

data class MemfaultState(
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
