package no.nordicsemi.memfault.lib.db

import no.nordicsemi.memfault.lib.data.Chunk

internal fun ChunkEntity.toChunk(): Chunk {
    return Chunk(chunkNumber, data, deviceId, isUploaded)
}

internal fun Chunk.toEntity(): ChunkEntity {
    return ChunkEntity(
        chunkNumber = chunkNumber,
        data = data,
        isUploaded = isUploaded,
        deviceId = deviceId
    )
}
