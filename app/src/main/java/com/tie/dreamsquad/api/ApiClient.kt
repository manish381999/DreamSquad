package com.tie.dreamsquad.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {

    private const val BASE_URL = "https://blanchedalmond-stinkbug-648195.hostingersite.com/api/"

    val api: ApiEndPointInterface by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiEndPointInterface::class.java)
    }
}