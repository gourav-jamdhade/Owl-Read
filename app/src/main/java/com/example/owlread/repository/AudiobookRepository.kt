package com.example.owlread.repository

import android.util.Log
import com.example.owlread.model.Audiobook
import com.example.owlread.network.RetrofitInstance

class AudiobookRepository {

    suspend fun getAudiobooks(
        title: String? = null,
        author: String? = null,
        offset: Int = 0
    ): List<Audiobook>? {
        return try {
            val response = RetrofitInstance.api.getAudiobooks(
                title = title,
                author = author,
                offset = offset
            )
//            val rawResponse = response.errorBody()?.string() ?: response.body().toString()
//            Log.d("RawResponse", rawResponse)
            if (response.isSuccessful) {
                val books =response.body()?.books?.filter { it.totaltime != "0" } ?: emptyList()
                books.forEach { book ->
                    Log.d("AudiobookRepository", "Book: ${book.title}, Total Time (sec): ${book.totaltimesecs}")
                }
                books
            } else {
                null
            }
        } catch (e: Exception) {
            Log.d("AudiobookRepo Exception", "getAudiobooks: ${e.localizedMessage}")
            null
        }
    }


}