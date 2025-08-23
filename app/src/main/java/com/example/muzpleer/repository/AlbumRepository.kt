package com.example.muzpleer.repository

import androidx.core.net.toUri
import com.example.muzpleer.model.Album
import com.example.muzpleer.room.dao.AlbumDao
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.entity.AlbumFile
import com.example.muzpleer.room.utils.fromSongFileToSong
import com.example.muzpleer.room.utils.toArtistList

class AlbumRepository(
    private val albumDao: AlbumDao,
    private val mediaDao: SongDao
) {
    suspend fun syncAlbumsFromMediaFiles() {
        // Получаем все медиафайлы
        val mediaFiles = mediaDao.getAllFiles()

        // Группируем по альбомам
        val albumsMap = mediaFiles
            .groupBy { it.albumId to it.album }
            .mapValues { (key, songs) ->
                val (albumId, albumName) = key
                val artists = songs.map { it.artist ?: "Unknown" }.distinct()

                AlbumFile(
                    mediaStoreId = albumId,
                    title = albumName ?: "Unknown Album",
                    artist = if (artists.size > 1) "Разные исполнители" else artists.first(),
                    allArtists = artists.joinToString(";"),
                    songCount = songs.size,
                    coverPath = songs.firstOrNull()?.artUri
                )
            }
        // Сохраняем в базу
        albumDao.insertAll(albumsMap.values.toList())
    }

//    suspend fun getAlbumWithSongs(albumId: Long): AlbumWithSongs {
//        val album = albumDao.getAlbumById(albumId) ?: throw Exception("Album not found")
//        val songs = mediaDao.getFilesByAlbumId(album.mediaStoreId)
//        return AlbumWithSongs(album, songs)
//    }

    suspend fun getAllAlbumsWithSongs(): List<Album> {
        val albums = albumDao.getAllAlbums()
        return albums.map { albumFile ->
            val songFileList = mediaDao.getFilesByAlbumId(albumFile.mediaStoreId)
            Album(
                id =albumFile.mediaStoreId,
                title = albumFile.title,
                artist =albumFile.artist ,
                artists = albumFile.allArtists.toArtistList(),
                artworkUri =(albumFile.coverPath)?.toUri(),
                albumId = albumFile.mediaStoreId,
                songs = fromSongFileToSong (songFileList)
            )
        }
    }
}