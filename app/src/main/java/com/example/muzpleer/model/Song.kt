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
    val artworkUri: Uri? = null, //обложка из local
    val album: String? = null, //название альбома из local
    val albumId: Long = -1, // id альбома из local
    val folderPath: String = "",  //путь к папке

    val typeFromIfMy: String = "",  //откуда трек, если он сгенерирован нейросетью
    val cover: Int? = null, //обложка из ресурсов
    val resourceId: Int? = null // мп3 для треков из ресурсов приложения
): Parcelable{

    fun getContentUri(): Uri {
        return ContentUris
            .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,id )
    }

    @IgnoredOnParcel
    private var cachedArtworkUri: Uri? = null

    fun getArtworkUri(context: Context): Uri {
        return cachedArtworkUri ?: run {
            val uri = when {
                albumId != -1L -> getArtworkUriFromMediaStore(albumId)
                else -> tryExtractFromFile(context)
            }
            cachedArtworkUri = uri
            uri
        }
    }

    fun getArtworkUriFromMediaStore(albumId: Long): Uri {
        return ContentUris.withAppendedId(
            "content://media/external/audio/albumart".toUri(),
            albumId
        )
    }

    private fun tryExtractFromFile(context: Context): Uri {
        return getEmbeddedArtwork(mediaUri)?.let { bitmap ->
            saveBitmapAndGetUri(context, bitmap)
        } ?: getDefaultArtworkUri(context)
    }

    fun getEmbeddedArtwork(path: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(path)
            val art = retriever.embeddedPicture
            if (art != null) BitmapFactory.decodeByteArray(art, 0, art.size)
            else null
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun saveBitmapAndGetUri(context: Context, bitmap: Bitmap): Uri {
        val file = File(context.cacheDir, "artwork_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }

    private fun getDefaultArtworkUri(context: Context): Uri{
        // Возвращаем дефолтную обложку
        return Uri.parse("android.resource://${context.packageName}/drawable/default_artwork")
    }

}

