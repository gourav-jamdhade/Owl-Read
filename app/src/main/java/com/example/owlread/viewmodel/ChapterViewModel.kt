package com.example.owlread.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlread.model.Chapter
import com.example.owlread.repository.ChapterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

class ChapterViewModel : ViewModel() {
    private val repository = ChapterRepository()
    private val _imageUrl = MutableStateFlow<String?>(null)
    val imageUrl: StateFlow<String?> get() = _imageUrl

    private val _chapters = MutableStateFlow<List<Chapter>?>(null)
    val chapters: StateFlow<List<Chapter>?> get() = _chapters

    private val _selectedChapter = MutableStateFlow<Chapter?>(null)
    //val selectedChapter: StateFlow<Chapter?> get() = _selectedChapter

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private var currentIndex = -1

    // Cache for chapters by audiobook ID
    private val chaptersCache = mutableMapOf<Int, Pair<String?, List<Chapter>?>>()



    fun fetchChaptersByAudiobookId(audiobookId: Int) {
        // Reset state before fetching new chapters

        if (chaptersCache.containsKey(audiobookId)) {
            // Use cached chapters
            try {
                Log.d("ChapterViewModel", "Using cached chapters for audiobook ID: $audiobookId")
                val (imageUrl, chapterList) = chaptersCache[audiobookId]!!
                _imageUrl.value = imageUrl
                _chapters.value = chapterList
            }catch (e:Exception){
                Log.d("ChapterViewModel Exception", "Error: ${e.message}")
            }


        } else {

            try {
//                _imageUrl.value = null
//                _chapters.value = null
                _isLoading.value = true
                viewModelScope.launch {
                    Log.d("ChapterViewModel", "Fetching chapters for audiobook ID: $audiobookId")
                    val (imageUrl, chapterList) = repository.getChaptersByAudiobookId(audiobookId)
                    chaptersCache[audiobookId] = Pair(imageUrl, chapterList) // Cache the result
                    Log.d(
                        "ChapterViewModel",
                        "fetchChaptersByAudiobookId: ${chaptersCache[audiobookId]?.second!![0].title}"
                    )

                    Log.d(
                        "ChapterViewModel",
                        "Chapters Cache Size: ${chaptersCache.size}"
                    )
                    Log.d("ChapterViewModel", "Cached chapters for audiobook ID: $audiobookId")
                    _imageUrl.value = imageUrl
                    _chapters.value = chapterList
                    _isLoading.value = false


                    if (!chapterList.isNullOrEmpty()) {
                        currentIndex = 0
                        _selectedChapter.value = chapterList[0]
                    }
                }
            }catch (e:Exception){
                Log.d("ChapterViewModel Exception", "Error: ${e.message}")
            }


        }
    }

    fun clearCache() {
        chaptersCache.clear()
        _imageUrl.value = null
        _chapters.value = null
        _isLoading.value = false
        _error.value = null
    }


    fun fetchChapters(rssUrl: String) {
        viewModelScope.launch {

            _isLoading.value = true
            _error.value = null // Clear previous errors
            try {
                val (imgUrl, chapterList) = repository.getChapters(rssUrl)
                _imageUrl.value = imgUrl
                _chapters.value = chapterList
                _isLoading.value = false
            } catch (e: SSLHandshakeException) {
                _error.value = "SSL Handshake Timeout"
                _isLoading.value = false
            } catch (e: SocketTimeoutException) {
                _error.value = "Network Timeout"
                _isLoading.value = false
            } catch (e: HttpException) {
                _error.value = "HTTP Error: ${e.code()}"
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "An unexpected error occurred."
                _isLoading.value = false
            }

        }
    }


    fun selectChapter(chapter: Chapter) {

        val chapterList = _chapters.value
        if (!chapterList.isNullOrEmpty()) {
            val newIndex =
                chapterList.indexOfFirst { it.enclosure?.url == chapter.enclosure?.url }

            if (newIndex != -1) { // Only update if the chapter is found
                currentIndex = newIndex
                _selectedChapter.value = chapter
            }
        }

        //currentIndex = _chapters.value?.indexOf(chapter) ?: -1
        Log.d(
            "ChapterViewModel",
            "Selected Chapter: ${chapter.title}, URL: ${chapter.enclosure?.url}"
        )

        Log.d("ChapterViewModel", "Current Index: $currentIndex")


    }

    // Helper function to get the next chapter
    fun getNextChapter(currentIndex: Int): Chapter? {
        return chapters.value?.let { chapters ->
            if (currentIndex < chapters.size - 1) {
                chapters[currentIndex + 1]
            } else {
                null
            }
        }
    }

    // Helper function to get the previous chapter
    fun getPreviousChapter(currentIndex: Int): Chapter? {
        return chapters.value?.let { chapters ->
            if (currentIndex > 0) {
                chapters[currentIndex - 1]
            } else {
                null
            }
        }
    }

//    fun skipNext() {
//        Log.d("ChapterViewModel", "skiNext called")
//        val chapterList = _chapters.value
//        if (chapterList != null && currentIndex in 0 until chapterList.size - 1) {
//            currentIndex++
//            _selectedChapter.postValue(chapterList[currentIndex])
//        } else {
//            Log.d("ChapterViewModel", "Skip Next: Reached the last chapter or invalid index")
//        }
//
//        Log.d("ChapterViewModel next", "Current Index: $currentIndex")
//    }
//
//    fun skipPrevious() {
//        Log.d("ChapterViewModel", "skipPrevious called")
//        if (currentIndex > 0) {
//            currentIndex--
//            _selectedChapter.postValue(_chapters.value?.get(currentIndex))
//        } else {
//            Log.d(
//                "ChapterViewModel",
//                "Skip Previous: Reached the first chapter or invalid index"
//            )
//        }
//
//        Log.d("ChapterViewModel prev", "Current Index: $currentIndex")
//    }
}
