package com.example.muzpleer.ui.tracks

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.model.PlaylistRepository

class KingsViewModel(val repository: PlaylistRepository): ViewModel(){

    val _tracks: MutableLiveData<List<MusicTrack>> = MutableLiveData(listOf<MusicTrack>())
    val tracks:LiveData<List<MusicTrack>> = _tracks

    init{
        loadData ()
    }

    fun loadData (){
        _tracks.value  = repository.getPlaylistKing()
    }
}