package com.nordicsemi.memfault

import com.nordicsemi.memfault.network.NetworkApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Module
@InstallIn(SingletonComponent::class)
class MemfaultModule {

    @Provides
    fun provideNetworkApi(): NetworkApi {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()

        return retrofit.create(NetworkApi::class.java)
    }
}
