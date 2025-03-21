package com.example.owlread.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.owlread.model.Audiobook
import com.example.owlread.repository.AudiobookRepository
import kotlinx.coroutines.launch

class AudiobookViewModel : ViewModel() {

    private val repository = AudiobookRepository()

    private val _audiobooks = MutableLiveData<List<Audiobook>>()
    val audiobooks: LiveData<List<Audiobook>> get() = _audiobooks


    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> get() = _isLoading

    private var _currentOffset = 0
    private var _currentAuthor: String? = null
    private var _sortByDuration: Boolean = false

    fun fetchAudiobooks(
        title: String? = null, author: String? = null, sortByDuration: Boolean = false
    ) {
        if (_isLoading.value == true) return
        _isLoading.value = true
        _currentAuthor = author
        _sortByDuration = sortByDuration


        viewModelScope.launch {
            try {
                val newBooks = repository.getAudiobooks(
                    title = title, author = author, offset = _currentOffset
                ) ?: emptyList()
                val currentBooks = _audiobooks.value.orEmpty()
                val uniqueNewBooks = newBooks.filter { newBook ->
                    currentBooks.none { it.id == newBook.id } // Filter out duplicates
                }

                // Combine current and new books
                val combinedBooks = currentBooks + uniqueNewBooks

                // Sort by duration if required
                val sortedBooks = if (sortByDuration) {
                    combinedBooks.sortedBy{ it.totaltimesecs }
                } else {
                    combinedBooks
                }

                sortedBooks.forEach { book ->
                    Log.d("AudiobookViewModel", "Sorted Book: ${book.title}, Total Time (sec): ${book.totaltimesecs}")
                }

                _audiobooks.value = sortedBooks
                _currentOffset += uniqueNewBooks.size // Increment offset by the number of unique books fetched
            } catch (e: Exception) {
                Log.d("Audiobook Exception", "${e.message}")
            } finally {
                _isLoading.value = false // Reset the loading state
            }
        }
    }



}