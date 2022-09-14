package com.nordicsemi.memfault.lib.data

import com.nordicsemi.memfault.lib.bluetooth.AuthorisationHeader

data class MemfaultConfig(
    val authorisationHeader: AuthorisationHeader,
    val url: String,
    val deviceId: String,
)
