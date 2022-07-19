package com.nordicsemi.memfault.dumping

data class StatsViewEntity(
    val chunks: Int = 0,
    val workingTime: Int = 0,
    val lastChunkUpdateTime: Int = 0
) {

    fun displayChunks() = "$chunks"

    fun displayWorkingTime() = "${workingTime}s"

    fun displayLastChunkUpdate() = "${getLastChunkUpdate()}s"

    private fun getLastChunkUpdate(): Int {
        return if (chunks > 0) {
            lastChunkUpdateTime
        } else {
            0
        }
    }
}
