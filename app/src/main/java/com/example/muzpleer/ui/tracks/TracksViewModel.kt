package com.example.muzpleer.ui.tracks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.model.PlaylistRepository


class TracksViewModel(val repository: PlaylistRepository): ViewModel(){

    var tracks: MutableLiveData<List<MusicTrack>> = MutableLiveData()

    init{
        loadData ()
    }

    fun loadData (){
        tracks.value  = repository.getPlaylist()
    }
}