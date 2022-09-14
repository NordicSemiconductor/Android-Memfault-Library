package com.nordicsemi.memfault.lib.bluetooth

import com.nordicsemi.memfault.lib.data.Chunk

internal class ChunkValidator {

    private var expectedChunk = 0 //FIXME initialise with latest received chunk number

    fun validateChunk(chunk: Chunk): Boolean {
        return if (chunk.number == expectedChunk) {
            expectedChunk++

            if (expectedChunk > 31) {
                expectedChunk = 0
            }
            true
        } else {
            false
        }
    }
}
