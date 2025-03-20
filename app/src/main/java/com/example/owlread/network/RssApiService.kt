package com.example.owlread.network

import com.example.owlread.model.RssFeed
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RssApiService {

    @GET
    suspend fun getChapters(@Url url:String):Response<RssFeed>
}

