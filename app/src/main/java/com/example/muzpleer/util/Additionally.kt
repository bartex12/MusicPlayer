package com.example.muzpleer.util

import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Folder
import com.example.muzpleer.model.Song

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