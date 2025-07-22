package com.example.muzpleer.model

import android.content.ContentUris
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import kotlinx.parcelize.Parcelize

@Parcelize
data class MusicTrack(
    val id: Long,  //id
    val title: String, //название
    val artist: String, //исполнитель
    val duration: Long,  //длительность трека
    val mediaUri: String,  // мп3 для треков из local
    val isLocal: Boolean = true,  //из ресурсов или local?
    val artworkUri: String? = null, //обложка из local
    val album: String? = null, //название альбома из local
    val albumId: Long = -1, // id альбома из local

    val typeFromIfMy: String = "",  //откуда трек, если он сгенерирован нейросетью
    val cover: Int? = null, //обложка из ресурсов
    val resourceId: Int? = null // мп3 для треков из ресурсов приложения
): Parcelable{
    fun getContentUri(): Uri {
        return ContentUris
            .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id )
    }
}

