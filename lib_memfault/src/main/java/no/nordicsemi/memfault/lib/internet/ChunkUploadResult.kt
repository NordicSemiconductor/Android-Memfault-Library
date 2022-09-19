package no.nordicsemi.memfault.lib.internet

internal sealed interface ChunkUploadResult

internal object ChunkUploadSuspended : ChunkUploadResult

internal data class ChunkUploadSuccess(val sent: Int) : ChunkUploadResult

internal data class ChunkUploadError(
    val delay: Long,
    val sent: Int,
    val exception: Exception
) : ChunkUploadResult
