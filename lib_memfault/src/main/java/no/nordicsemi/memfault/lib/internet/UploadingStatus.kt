package no.nordicsemi.memfault.lib.internet

sealed interface UploadingStatus {
    object Offline : UploadingStatus

    object InProgress : UploadingStatus

    data class Suspended(val delayInSeconds: Long) : UploadingStatus
}
