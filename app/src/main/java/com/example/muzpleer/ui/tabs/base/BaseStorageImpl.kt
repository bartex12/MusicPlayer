package com.example.muzpleer.ui.tabs.base

import android.util.Log
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.model.MyRepository
import com.example.muzpleer.model.PlaylistRepository


class BaseStorageImpl(private val repository:MyRepository): BaseStorage {
    override suspend fun getMyTracksList(): List<MusicTrack> {
        return repository.getMyTracksList()
    }

    companion object{const val TAG = "33333"}
}