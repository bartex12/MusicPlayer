package com.example.muzpleer.ui.local

import android.content.Context
import android.util.Log
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


class LocalMusicViewModel(
    private val mediaScanner: MediaScanner
) : ViewModel() {

companion object{
    const val TAG ="33333"
}

    private val  _progress= MutableLiveData<Boolean>(false)
    val progress: LiveData<Boolean> = _progress

    private val _musicList = MutableStateFlow<List<MusicTrack>>(emptyList())
    val musicList: StateFlow<List<MusicTrack>> = _musicList

    fun loadLocalMusic() {
        viewModelScope.launch {
            _progress.value = true
            Log.d(TAG, "#LocalMusicViewModel loadLocalMusic: _progress = true  ")
            withContext(Dispatchers.IO) {
                _musicList.value = mediaScanner.scanDeviceForMusic()
            }
        }
    }

    fun setProgress(isShow: Boolean){
        _progress.value = isShow
    }
}