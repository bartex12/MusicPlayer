package com.example.muzpleer.model

import android.os.Parcelable
import com.example.muzpleer.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicTrack(
    val id: Int,  //id
    val title: String, //название
    val artist: String, //исполнитель
    val duration: Long,  //длительность трека
    val mediaUri: String,  // мп3 для треков из local
    val isLocal: Boolean,  //из ресурсов или local?
    val artworkUri: String? = null, //обложка из local
    val album: String? = null, //название альбома из local
    val cover: Int? = null, //обложка из ресурсов
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

