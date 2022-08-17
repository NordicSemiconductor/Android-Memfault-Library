package com.nordicsemi.memfault.lib.bluetooth

internal class ChunkValidator {

    private var expectedChunk = 0

    fun validateChunk(chunk: Int) {
        if (chunk == expectedChunk) {
            expectedChunk++

            if (expectedChunk > 31) {
                expectedChunk = 0
            }
        } else {
            throw IllegalArgumentException("Expected chunk: $expectedChunk, but current: $chunk")
        }
    }
}
