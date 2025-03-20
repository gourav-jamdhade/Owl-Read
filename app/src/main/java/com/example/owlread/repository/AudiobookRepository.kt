package com.example.owlread.repository

import android.util.Log
import com.example.owlread.model.Audiobook
import com.example.owlread.network.RetrofitInstance
import com.example.owlread.network.RssRetrofitInstance

class AudiobookRepository {

    suspend fun getAudiobooks(title: String? = null, author: String? = null): List<Audiobook>? {
        return try {
            val response = RetrofitInstance.api.getAudiobooks(title, author)
            val rawResponse = response.errorBody()?.string() ?: response.body().toString()
            Log.d("RawResponse", rawResponse)
            if (response.isSuccessful) {
                response.body()?.books
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("AudiobookRepo Exception", "getAudiobooks: ${e.localizedMessage}")
            null
        }
    }


}