package com.example.muzpleer.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

data class MusicArtist(
    val name: String,
    val tracks: List<MusicTrack>,
    val artworkUri: Uri
)