package com.example.muzpleer.ui.my.storage

import com.example.muzpleer.model.Song
import com.example.muzpleer.repository.MyRepository
import com.example.muzpleer.ui.my.storage.MyStorage

class MyStorageImpl(private val repository: MyRepository): MyStorage {
    override suspend fun getMyTracksList(): List<Song> {
        return repository.getMyTracksList()
    }

    companion object{const val TAG = "33333"}
}