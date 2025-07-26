package com.example.muzpleer.ui.local.frags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.scaner.MediaScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LocalViewModel(
    private val mediaScanner: MediaScanner
) : ViewModel() {

    private val _musicList = MutableLiveData<List<MusicTrack>>(emptyList())
    val musicList: LiveData<List<MusicTrack>> = _musicList

    init {
        loadLocalMusic()
    }

    fun loadLocalMusic() {
        viewModelScope.launch {
            _musicList.value = mediaScanner.scanDeviceForMusic()
        }
    }

    companion object{
        const val TAG ="33333"
    }

}