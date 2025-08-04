package com.example.muzpleer.ui.tabs.base

import com.example.muzpleer.model.Song

interface MyStorage {
    suspend  fun getMyTracksList(): List<Song>

}