package com.example.muzpleer.model

import android.os.Parcelable
import com.example.muzpleer.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicTrack(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val mediaUri: String,
    val isLocal: Boolean,
    val artworkUri: String? = null,
    val album: String? = null,
    val cover: Int? = null,
    val resourceId: Int? = null // мп3 для треков из ресурсов приложения
): Parcelable{

//    companion object {
//        // Создание MediaItem из URI
//        fun fromUri(uri: String, title: String = "Unknown", artist: String = "Unknown"): MusicTrack {
//            return MusicTrack(
//                //id = Uri.parse(uri).lastPathSegment ?: System.currentTimeMillis().toString(),
//                title = title,
//                artist = artist,
//                cover = R.drawable.blowball,
//                music = R.raw.blowball,
//            )
//        }
//    }
}

