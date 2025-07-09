package com.example.muzpleer.ui.tracks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.muzpleer.model.MediaItemApp
import com.example.muzpleer.model.getPlayList

class TracksViewModel: ViewModel(){

    var tracks: MutableLiveData<List<MediaItemApp>> = MutableLiveData()

    fun loadData (){
        tracks.value = getPlayList()
    }
}