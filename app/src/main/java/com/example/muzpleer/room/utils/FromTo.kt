package com.example.muzpleer.room.utils

import com.example.muzpleer.model.Song
import com.example.muzpleer.room.entity.SongFile

fun fromSongFileToSong(songFiles:List<SongFile>):List<Song>{
    return songFiles.map{songFile->
        Song(
            id = songFile.mediaStoreId ,
            title =songFile.title.toString(),
            artist =songFile.artist.toString(),
            duration = songFile.duration ,
            mediaUri = songFile.path ,
            artUri = songFile.artUri ,
            isLocal = songFile.isLocal ,
            albumName = songFile.album ,
            albumId = songFile.albumId ,
            folderPath = songFile.folderPath
        )
    }
}

//fun fromAlbumFileToAlbum(albumFiles:List<AlbumFile>):List<Album>{
//    return albumFiles.map{albumFile->
//        Album(
//            id =albumFile.mediaStoreId,
//            title = albumFile.title,
//            artist =albumFile.artist ,
//            artists = albumFile.allArtists.toArtistList(),
//            artworkUri =(albumFile.coverPath)?.toUri(),
//            albumId = albumFile.mediaStoreId,
//            songs = fromSongFileToSong (mediaDao.getFilesByAlbumId(albumFile.mediaStoreId))
//        )
//    }
//}

fun String.toArtistList(): List<String> {
    return if (this.isBlank()) {
        emptyList()
    } else {
        this.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() && it != "Unknown" }
    }
}