package com.example.muzpleer.ui.tabs.base

import com.example.muzpleer.model.Song
import com.example.muzpleer.model.MyRepository


class MyStorageImpl(private val repository:MyRepository): MyStorage {
    override suspend fun getMyTracksList(): List<Song> {
        return repository.getMyTracksList()
    }

    companion object{const val TAG = "33333"}
}