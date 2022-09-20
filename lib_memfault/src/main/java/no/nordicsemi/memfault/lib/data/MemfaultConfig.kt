package no.nordicsemi.memfault.lib.data

import no.nordicsemi.memfault.lib.bluetooth.AuthorisationHeader

data class MemfaultConfig(
    val authorisationHeader: AuthorisationHeader,
    val url: String,
    val deviceId: String,
)
