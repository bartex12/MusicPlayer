package com.example.muzpleer.util

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.muzpleer.R
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Folder
import com.example.muzpleer.model.Playlist
import com.example.muzpleer.model.Song
import com.example.muzpleer.ui.local.adapters.SongSelectionAdapter.Companion.TAG
import com.example.muzpleer.ui.local.adapters.getFilePathFromUri
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

const val TAG = "33333"

fun getTracksCountString(count: Int): String {
    return when {
        count % 100 in 11..14 -> "$count треков"
        count % 10 == 1 -> "$count трек"
        count % 10 in 2..4 -> "$count трека"
        else -> "$count треков"
    }
}

fun getAlbumsCountString(count: Int): String {
    return when {
        count % 100 in 11..14 -> "$count альбомов "
        count % 10 == 1 -> "$count альбом"
        count % 10 in 2..4 -> "$count альбома"
        else -> "$count альбомов"
    }
}

fun getSortedDataFolder( folders:List<Folder>):List<Folder>{
    return folders.sortedWith(compareBy(
        { folder -> when {
            folder.name.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
            folder.name.matches(Regex("^[a-zA-Z].*")) -> 1
            else -> 2}
        },
        { folder -> folder.name.lowercase() }
    )
    )
}
fun getSortedDataArtist(artists:List<Artist>):List<Artist>{
    return artists.sortedWith(compareBy(
        { artist -> when {
            artist.name.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
            artist.name.matches(Regex("^[a-zA-Z].*")) -> 1
            else -> 2}
        },
        { artist -> artist.name.lowercase() }
    )
    )
}

fun getSortedDataAlbum(tracks:List<Album>):List<Album>{
    return tracks.sortedWith(compareBy(
        { album -> when {
            album.title.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
            album.title.matches(Regex("^[a-zA-Z].*")) -> 1
            else -> 2}
        },
        { album -> album.title.lowercase() }
    )
    )
}

fun getSortedDataPlaylists(tracks:List<Playlist>):List<Playlist>{
    return tracks.sortedWith(compareBy(
        { playlist -> when {
            playlist.playlistName.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
            playlist.playlistName.matches(Regex("^[a-zA-Z].*")) -> 1
            else -> 2}
        },
        { playlist -> playlist.playlistName.lowercase() }
    ))
}

fun getSortedDataSong(tracks:List<Song>):List<Song>{
    return tracks.sortedWith(compareBy(
        { track -> when {
            track.title.matches(Regex("^[а-яА-ЯёЁ].*")) -> 0
            track.title.matches(Regex("^[a-zA-Z].*")) -> 1
            else -> 2}
        },
        { track -> track.title.lowercase() }
    )
    )
}

// Добавьте в SongsAdapter или в отдельный файл Utils.kt
fun formatFileSize(size: Long): String {
    return when {
        size >= 1024 * 1024 -> "${String.format("%.1f", size / (1024.0 * 1024.0))} MB"
        size >= 1024 -> "${String.format("%.1f", size / 1024.0)} KB"
        else -> "$size B"
    }
}

fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Неизвестно"
    }
}

fun formatDuration(duration: Long): String {
    val seconds = duration / 1000
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

//функция для нормализации URI
fun getNormalizedPath(uriString: String): String {
    return if (uriString.startsWith("file://")) {
        Uri.decode(uriString.substring(7))
    } else {
        uriString
    }
}

fun showCoverImageWithGlide(context: Context, artUri: Uri?, imageView: ImageView){
    when {
        artUri == null -> {
           // Log.d(TAG, "1 Additionally showCoverImageWithGlide: artUri == null ")
            // Загрузка стандартной обложки
            Glide.with(context)
                .load(R.drawable.muz_player5)
                .into(imageView)
        }
        artUri.scheme == "content" && artUri.authority == "com.android.providers.downloads.documents" -> {
         //   Log.d(TAG, "2 Additionally showCoverImageWithGlide: artUri.scheme == \"content\"")
            // Обработка специальных content URI
            loadDownloadDocumentUri(context, artUri, imageView)
        }
        artUri.toString().contains("albumart") -> {
          //  Log.d(TAG, "3 Additionally showCoverImageWithGlide: artUri.toString().contains(\"albumart\")")
            // Стандартные album art URI
            Glide.with(context)
                .load(artUri)
                .placeholder(R.drawable.muz_player5)
                .error(R.drawable.muz_player5)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
        else -> {
            // Все остальные URI
          //  Log.d(TAG, "4 Additionally showCoverImageWithGlide: Все остальные URI")
            Glide.with(context)
                .load(artUri)
                .placeholder(R.drawable.muz_player5)
                .error(R.drawable.muz_player5)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
    }
}
private fun loadDownloadDocumentUri(context: Context, uri: Uri, imageView: ImageView) {
    try {
        // Пытаемся получить реальный путь файла
        val filePath = getFilePathFromUri(context, uri)
        if (filePath != null) {
            Glide.with(context)
                .load(File(filePath))
                .placeholder(R.drawable.muz_player3)
                .error(R.drawable.muz_player3)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        } else {
            // Fallback: пытаемся загрузить через ContentResolver
            Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.muz_player3)
                .error(R.drawable.muz_player3)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView)
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error loading download document URI: ${e.message}")
        Glide.with(context)
            .load(R.drawable.muz_player3)
            .into(imageView)
    }
}