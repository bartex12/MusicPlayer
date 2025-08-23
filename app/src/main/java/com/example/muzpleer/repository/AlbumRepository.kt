package com.example.muzpleer.repository

import android.util.Log
import androidx.core.net.toUri
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Song
import com.example.muzpleer.room.dao.AlbumDao
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.entity.AlbumFile
import com.example.muzpleer.room.entity.SongFile
import com.example.muzpleer.room.utils.fromSongFileToSong
import com.example.muzpleer.room.utils.toArtistList

class AlbumRepository(
    private val albumDao: AlbumDao,
    private val mediaDao: SongDao
) {
    companion object{
        const val TAG = "33333"
    }

    suspend fun syncAlbumsFromMediaFiles() {
        // Получаем все медиафайлы
        val mediaFiles = mediaDao.getAllFiles()

        // Группируем по альбомам
        val albumsMap =
            mediaFiles.groupBy { it.albumId to it.album }
                .mapValues { (key, songs) ->
                    val (albumId, albumName) = key
                    val artists = songs.map { it.artist ?: "Unknown" }.distinct()
                AlbumFile(
                    albumId = albumId,
                    title = albumName ?: "Неизвестный альбом",
                    artist = if (artists.size > 1) "Разные исполнители" else artists.first(),
                    allArtists = artists.joinToString(";"),
                    songCount = songs.size,
                    coverPath = songs.firstOrNull()?.artUri
                )
            }

        try {
            // Сохраняем в базу
            albumDao.insertAll(albumsMap.values.toList())
        }catch (e: Exception){
            Log.d(TAG, "*AlbumRepository syncAlbumsFromMediaFiles Exception = ${e.message}")
        }
        val albumSize = albumDao.getAllAlbums().size
        Log.d(TAG, "*AlbumRepository syncAlbumsFromMediaFiles albumsMap size = ${albumsMap.size}" +
                "albumSize = $albumSize")
    }

//    suspend fun getAlbumWithSongs(albumId: Long): AlbumWithSongs {
//        val album = albumDao.getAlbumById(albumId) ?: throw Exception("Album not found")
//        val songs = mediaDao.getFilesByAlbumId(album.mediaStoreId)
//        return AlbumWithSongs(album, songs)
//    }

    suspend fun getAllAlbumsWithSongs(): List<Album> {
        val albums = albumDao.getAllAlbums() //получаем список альбомов из базы
        return albums.map { albumFile ->
            val songFileList = mediaDao.getFilesByAlbumId(albumFile.albumId)
            //Log.d(TAG, "*AlbumRepository getAllAlbumsWithSongs songFileList size = ${songFileList.size}")
            Album(
                id =albumFile.albumId,
                title = albumFile.title,
                artist =albumFile.artist ,
                artists = albumFile.allArtists.toArtistList(),
                artworkUri =(albumFile.coverPath)?.toUri(),
                albumId = albumFile.albumId,
                songs = fromSongFileToSong (songFileList)
            )
        }
    }

    suspend fun getAlbumSongList(albumId: Long): List<Song> {
        var songFileList: List<SongFile> = listOf()
        val allAlbums = albumDao.getAllAlbums()
        Log.d(TAG, "*AlbumRepository getAlbumSongList allAlbums size   = ${allAlbums.size} ")
        val allIds = albumDao. getAllIds()
        Log.d(TAG, "*AlbumRepository getAlbumSongList getAllIds = $allIds ")
         val albumFile: AlbumFile? = albumDao.getAlbumById(albumId)
        Log.d(TAG, "*AlbumRepository getAlbumSongList albumFile  = $albumFile")
        if (albumFile!= null){
            songFileList = mediaDao.getFilesByAlbumId(albumFile.albumId)
            Log.d(TAG, "*AlbumRepository getAlbumSongList songFileList size = ${songFileList.size}")
        }
        return fromSongFileToSong(songFileList)
    }
}