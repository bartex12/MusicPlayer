package com.example.muzpleer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.muzpleer.model.MusicTrack

const val  TAG = "33333"

class SharedViewModel():ViewModel(){
    private val _playlist: MutableLiveData<List<MusicTrack>> = MutableLiveData<List<MusicTrack>>(emptyList())
    val playlist:LiveData<List<MusicTrack>> = _playlist

    fun setPlaylist(tracks:List<MusicTrack>){
        _playlist.value = tracks
        Log.d(TAG, "### SharedViewModel setPlaylist: size = ${playlist.value?.size} ")
    }

    fun getPlaylist():List<MusicTrack> {
        Log.d(TAG, "### SharedViewModel getPlaylist: size = ${playlist.value?.size} ")
        return playlist.value
    }
}