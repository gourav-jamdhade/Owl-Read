package com.example.owlread.network

import com.example.owlread.model.LibrivoxResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LibrivoxApiService {

    @GET("feed/audiobooks/?format=json&limit=10")
    suspend fun getAudiobooks(

        @Query("title") title: String? = "",
        @Query("author") author: String? = "",
        @Query("totaltime") totaltime: String? = ""
    ): Response<LibrivoxResponse>


}