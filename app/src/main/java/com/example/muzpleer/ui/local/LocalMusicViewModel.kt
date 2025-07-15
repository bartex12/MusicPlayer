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

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted

    fun setPermissionGranted(granted: Boolean) {
        _permissionGranted.value = granted
        Log.d(TAG, "LocalMusicViewModel setPermissionGranted:granted = $granted  ")
        if (granted) {
            loadLocalMusic()
        }
    }

    private fun loadLocalMusic() {
        viewModelScope.launch {
            _musicList.value = mediaScanner.scanDeviceForMusic()
        }
    }
}