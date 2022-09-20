package no.nordicsemi.memfault.lib.internet

class UploadSuspendedException : Exception("Upload manager is currently suspended due to the server overload.")
