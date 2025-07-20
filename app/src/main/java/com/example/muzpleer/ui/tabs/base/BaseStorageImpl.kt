package com.example.muzpleer.ui.tabs.base

import android.util.Log
import com.example.muzpleer.model.MusicTrack


class BaseStorageImpl(): BaseStorage {
    override suspend fun getMyTracksList(): List<MusicTrack> {
        return getMyTracksList()
    }

    companion object{const val TAG = "33333"}
}