package com.example.owlread.network

import com.example.owlread.model.Audiobook
import com.example.owlread.model.LibrivoxResponse
import org.simpleframework.xml.Path
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface LibrivoxApiService {

    @GET("feed/audiobooks/?format=json")
    suspend fun getAudiobooks(

        @Query("title") title: String? = "",
        @Query("author") author: String? = "",
        @Query("limit") limit: Int = 10,// Add page for pagination
        @Query("offset") offset: Int = 0,// Add page for pagination
    ): Response<LibrivoxResponse>


    // Add a new endpoint to fetch a single book by its ID
    @GET("feed/audiobooks/?format=json")
    suspend fun getAudiobookById(@Query("id") id: Int=0): Response<LibrivoxResponse>
}