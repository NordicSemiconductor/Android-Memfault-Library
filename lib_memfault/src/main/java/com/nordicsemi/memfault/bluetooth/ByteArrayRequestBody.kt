package com.nordicsemi.memfault.bluetooth

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink

class ByteArrayRequestBody(private val data: ByteArray) : RequestBody() {

    override fun contentType(): MediaType? {
        return "application/octet-stream".toMediaTypeOrNull()
    }

    override fun writeTo(sink: BufferedSink) {
        sink.write(data)
    }

    override fun contentLength(): Long {
        return data.size.toLong()
    }
}
