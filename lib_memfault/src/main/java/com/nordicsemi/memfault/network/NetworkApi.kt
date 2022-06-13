package com.nordicsemi.memfault.network

import retrofit2.http.Body
import retrofit2.http.POST

interface NetworkApi {

    @POST("users/new") //TODO change API
    suspend fun createUser(@Body user: String)
}
