package com.example.muzpleer.model

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.content.FileProvider
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import androidx.core.net.toUri
import java.io.File
import java.io.FileOutputStream

@Parcelize
data class Song(
    val id: Long,  //id
    val title: String, //название
    val artist: String, //исполнитель
    val duration: Long,  //длительность трека
    val mediaUri: String,  // мп3 для треков из local
    val isLocal: Boolean = true,  //из ресурсов или local?
    val album: String? = null, //название альбома из local
    val albumId: Long = -1, // id альбома из local
    val folderPath: String = "",  //путь к папке
): Parcelable{

    fun getContentUri(): Uri {
        return ContentUris
            .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id )
    }

}

