package com.example.owlread.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.util.concurrent.TimeUnit

object RssRetrofitInstance {

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
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