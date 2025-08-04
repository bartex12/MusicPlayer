package com.example.muzpleer.ui.tabs.base

import com.example.muzpleer.model.Song

interface BaseStorage {
    suspend  fun getMyTracksList(): List<Song>

}