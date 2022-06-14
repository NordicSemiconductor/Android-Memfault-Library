package com.nordicsemi.memfault.network

import retrofit2.http.*

interface NetworkApi {

    @Headers("Content-Type: application/octet-stream")
    @POST
    suspend fun sendLog(
        @Url url: String,
        @Body user: String
    )
}
