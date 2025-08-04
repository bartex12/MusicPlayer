package com.example.muzpleer.model

import android.net.Uri

data class Artist(
    val name: String,
    val tracks: List<Song>,
    val artworkUri: Uri
)