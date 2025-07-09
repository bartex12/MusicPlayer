package com.example.muzpleer.model

import android.net.Uri
import android.os.Parcelable
import com.example.muzpleer.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class MediaItem(
    //val id: String,
    val title: String,
    val artist: String,
    val cover:Int,
    val music:Int,
    //val album: String? = null,
    //val duration: Long,
    //val mediaUri: String,
    //val isLocal: Boolean,
    //val artworkUri: String? = null

): Parcelable{

    companion object {
        // Создание MediaItem из URI
        fun fromUri(uri: String, title: String = "Unknown", artist: String = "Unknown"): MediaItem {
            return MediaItem(
                //id = Uri.parse(uri).lastPathSegment ?: System.currentTimeMillis().toString(),
                title = title,
                artist = artist,
                cover = R.drawable.bohemian,
                music = R.raw.bohemian,
               // duration = 0,
               // mediaUri = uri,
               // isLocal = uri.startsWith("content://") || uri.startsWith("file://")
            )
        }
    }
}

