package com.example.owlread.repository

import android.util.Log
import com.example.owlread.model.Chapter
import com.example.owlread.network.RetrofitInstance
import com.example.owlread.network.RssRetrofitInstance
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.net.ssl.SSLHandshakeException

class ChapterRepository {

    suspend fun getChaptersByAudiobookId(audiobookId: Int): Pair<String?, List<Chapter>?> {
        return try {
            Log.d("ChapterRepository", "Fetching chapters for audiobook ID: $audiobookId")
            val response = RetrofitInstance.api.getAudiobookById(audiobookId)
            if (response.isSuccessful) {
                val audiobook = response.body()?.books?.firstOrNull()
                Log.d("ChapterRepository", "Audio Title: ${response.body()}")
                Log.d("ChapterRepository", "Audio Title: ${audiobook?.title}")
                Log.d("ChapterRepository", "Audio Author: ${audiobook?.authors?.get(0)?.fullName}")

                val rssUrl = audiobook?.url_rss

                Log.d("ChapterRepository Successful", "RSS URL: $rssUrl")
                if (!rssUrl.isNullOrEmpty()) {
                    getChapters(rssUrl)
                } else {
                    Pair(null, emptyList())
                }
            } else {
                Log.e("ChapterRepository", "Error: ${response.code()}")
                Pair(null, emptyList())
            }
        } catch (e: Exception) {
            Log.e("ChapterRepository", "${e.message}")
            Pair(null, emptyList())
        }
    }


    suspend fun getChapters(url: String): Pair<String?, List<Chapter>?> {
        return try {
            val decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8.toString())

            val response = RssRetrofitInstance.api.getChapters(decodedUrl)
            if (response.isSuccessful) {
                Log.d(
                    "ChapterRepository",
                    "Parsed duration: ${response.body()?.channel?.items?.get(0)?.duration}"
                )
                Log.d("ChapterRepository", "Full RSS XML: $response")
//                Log.d("ChapterRepository", "Full RSS XML: ${response.body()?.channel?.items?.get(0)?.imageUrl}")
                Log.d(
                    "ChapterRepository",
                    "Audio URL: ${response.body()?.channel?.items?.get(0)?.enclosure?.url}"
                )
                val imageUrl = response.body()?.channel?.itunesImage?.href
                Log.d("ChapterRepository", "Image URL: $imageUrl")
                val chapters = response.body()?.channel?.items ?: emptyList()
                Pair(imageUrl, chapters)

            } else {
                Log.e("ChapterRepository", "Fetching URL: $decodedUrl")
                Log.e("ChapterRepository", "Error: ${response.code()}")
                return Pair(null, emptyList())
            }
        } catch (e: SSLHandshakeException) {
            Log.e("ChapterRepository SSL", "SSL Handshake Timeout: ${e.message}")
            // Show SSL timeout error message to the user
            // Clear shimmer, show retry button
            return Pair(null, emptyList())
        } catch (e: SocketTimeoutException) {
            Log.e("ChapterRepository Timeout", "Socket Timeout: ${e.message}")
            // Show timeout error message to the user
            // Clear shimmer, show retry button
            return Pair(null, emptyList())
        } catch (e: HttpException) {
            Log.e("ChapterRepository HTTP", "${e.message}")
            return Pair(null, emptyList())
        } catch (e: Exception) {
            Log.e("ChapterRepository", "${e.message}")
            return Pair(null, emptyList())
        }

    }
}