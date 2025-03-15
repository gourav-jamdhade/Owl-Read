package com.example.owlread.viewmodel

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

    fun fetchAudiobooks(title: String? = null, author: String? = null) {
        viewModelScope.launch {
            _audiobooks.value = repository.getAudiobooks(title, author)

        }
    }
}