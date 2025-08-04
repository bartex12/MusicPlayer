package com.example.muzpleer.model

import android.content.ContentUris
import android.net.Uri
import android.provider.MediaStore

data class Folder(
    val path: String,
    val name: String,
    val tracks: List<Song>,
    val artworkUri: Uri? =  ContentUris
        .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            tracks.firstOrNull()?.albumId ?: -1
        )
)