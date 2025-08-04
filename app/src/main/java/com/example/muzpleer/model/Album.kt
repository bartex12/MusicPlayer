package com.example.muzpleer.model

import android.net.Uri

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val artworkUri: Uri? = null,
    val tracks: List<Song> = emptyList()
)