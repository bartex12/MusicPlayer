package com.example.muzpleer.ui.local.frags

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.muzpleer.model.Song
import com.example.muzpleer.scaner.MediaScanner
import kotlinx.coroutines.launch

class LocalViewModel(
    private val mediaScanner: MediaScanner
) : ViewModel() {

    private val _musicList = MutableLiveData<List<Song>>(emptyList())
    val musicList: LiveData<List<Song>> = _musicList

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