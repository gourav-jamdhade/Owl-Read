package com.example.owlread.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

object RssRetrofitInstance {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS) // Reduced timeout for testing
        .readTimeout(10, TimeUnit.SECONDS)    // Reduced timeout for testing
        .writeTimeout(10, TimeUnit.SECONDS)   // Reduced timeout for testing
        .build()

    private val retrofit:Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://librivox.org/")
            .client(okHttpClient)
            .addConverterFactory(SimpleXmlConverterFactory.create())
            .build()
    }

    val api:RssApiService by lazy {
        retrofit.create(RssApiService::class.java)
    }
}