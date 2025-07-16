package com.example.muzpleer.ui.local

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.scaner.MediaScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch



class LocalMusicViewModel(
    private val mediaScanner: MediaScanner
) : ViewModel() {

companion object{
    const val TAG ="33333"
}

    private val _musicList = MutableStateFlow<List<MusicTrack>>(emptyList())
    val musicList: StateFlow<List<MusicTrack>> = _musicList

    fun loadLocalMusic() {
        viewModelScope.launch {
            _musicList.value = mediaScanner.scanDeviceForMusic()
        }
    }
}