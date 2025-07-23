package com.example.muzpleer.ui.local.frags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.scaner.MediaScanner
import kotlinx.coroutines.launch


class FolderViewModel(
    private val mediaScanner: MediaScanner
) : ViewModel() {

    companion object {
        const val TAG = "33333"
    }

    private val _musicList = MutableLiveData<List<MusicTrack>>(emptyList())
    val musicList: LiveData<List<MusicTrack>> = _musicList

    fun loadLocalMusic() {
        viewModelScope.launch {
            _musicList.value  = mediaScanner.scanDeviceForMusic()
        }
    }
}