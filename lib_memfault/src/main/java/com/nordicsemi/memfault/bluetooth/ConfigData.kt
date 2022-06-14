package com.nordicsemi.memfault.bluetooth

sealed interface MemfaultEntity

object MemfaultDataNotAvailableEntity : MemfaultEntity

data class MemfaultDataEntity(
    val config: ConfigData,
    val message: String
) : MemfaultEntity

data class ConfigData(
    val authorisation: AuthorisationHeader,
    val deviceId: String,
    val url: String
)

data class AuthorisationHeader(
    val header: String
) {

    val key: String
    val value: String

    init {
        val components = header.split(":")
        key = components[0]
        value = components[1]
    }
}
