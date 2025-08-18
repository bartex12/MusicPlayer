package com.example.muzpleer.model

import android.net.Uri

data class Folder(
    val path: String,
    val name: String,
    val songs: List<Song>,
    val artworkUri : Uri? = null
)