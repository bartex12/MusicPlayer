package com.example.muzpleer.room.utils

import com.example.muzpleer.model.Song
import com.example.muzpleer.room.entity.SongFile

fun FromSongFileToSong(songFiles:List<SongFile>):List<Song>{
    return songFiles.map{songFile->
        Song(
            id = songFile.mediaStoreId ,
            title =songFile.title.toString(),
            artist =songFile.artist.toString(),
            duration = songFile.duration ,
            mediaUri = songFile.path ,
            artUri = songFile.artUri ,
            isLocal = songFile.isLocal ,
            album = songFile.album ,
            albumId = songFile.albumId ,
            folderPath = songFile.folderPath
        )
    }
}