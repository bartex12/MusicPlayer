package com.example.muzpleer.ui.tabs.base

import com.example.muzpleer.model.MusicTrack

interface BaseStorage {
    suspend  fun getMyTracksList(): List<MusicTrack>

}