package com.example.owlread.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlread.model.Chapter
import com.example.owlread.repository.ChapterRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

class ChapterViewModel : ViewModel() {
    private val repository = ChapterRepository()
    private val _imageUrl = MutableLiveData<String?>()
    val imageUrl: MutableLiveData<String?> get() = _imageUrl

    private val _chapters = MutableLiveData<List<Chapter>?>()
    val chapters: LiveData<List<Chapter>?> get() = _chapters

    private val _selectedChapter = MutableLiveData<Chapter?>()
    val selectedChapter: LiveData<Chapter?> get() = _selectedChapter

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var currentIndex = -1


    fun fetchChaptersByAudiobookId(audiobookId: Int) {
        viewModelScope.launch {
            val (imageUrl, chapterList) = repository.getChaptersByAudiobookId(audiobookId)
            _imageUrl.postValue(imageUrl)
            _chapters.postValue(chapterList)

            if (!chapterList.isNullOrEmpty()) {
                currentIndex = 0  // Set first chapter by default
                _selectedChapter.postValue(chapterList[0])
            }
        }
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

    fun skipNext() {
        Log.d("ChapterViewModel", "skiNext called")
        val chapterList = _chapters.value
        if (chapterList != null && currentIndex in 0 until chapterList.size - 1) {
            currentIndex++
            _selectedChapter.postValue(chapterList[currentIndex])
        } else {
            Log.d("ChapterViewModel", "Skip Next: Reached the last chapter or invalid index")
        }

        Log.d("ChapterViewModel next", "Current Index: $currentIndex")
    }

    fun skipPrevious() {
        Log.d("ChapterViewModel", "skipPrevious called")
        if (currentIndex > 0) {
            currentIndex--
            _selectedChapter.postValue(_chapters.value?.get(currentIndex))
        } else {
            Log.d(
                "ChapterViewModel",
                "Skip Previous: Reached the first chapter or invalid index"
            )
        }

        Log.d("ChapterViewModel prev", "Current Index: $currentIndex")
    }
}
