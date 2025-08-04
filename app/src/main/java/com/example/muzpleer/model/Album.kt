package com.example.muzpleer.model

import android.net.Uri

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artworkUri: Uri? = null,
    val songs: List<Song> = emptyList()
)