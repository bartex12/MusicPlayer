package com.example.muzpleer.ui.my.storage

import com.example.muzpleer.model.Song

interface MyStorage {
    suspend  fun getMyTracksList(): List<Song>

}