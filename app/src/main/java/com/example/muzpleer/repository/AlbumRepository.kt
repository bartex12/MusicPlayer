package com.example.muzpleer.repository

import android.util.Log
import androidx.core.net.toUri
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Song
import com.example.muzpleer.room.dao.AlbumDao
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.entity.AlbumFile
import com.example.muzpleer.room.entity.ArtistFile
import com.example.muzpleer.room.entity.SongFile
import com.example.muzpleer.room.utils.fromSongFileListToSongList
import com.example.muzpleer.room.utils.toArtistList

class AlbumRepository(
    private val albumDao: AlbumDao,
    private val mediaDao: SongDao
) {
    companion object{
        const val TAG = "33333"
    }

    suspend fun syncAlbumsFromMediaFiles() {

        // СОХРАНЯЕМ КАСТОМНЫЕ ОБЛОЖКИ ПЕРЕД ОЧИСТКОЙ
        val existingAlbum = albumDao.getAllAlbums()
        val customCoversMap = mutableMapOf<String, String>() // // albumKey -> coverPath

        existingAlbum.forEach { album ->
            if (!album.coverPath.isNullOrEmpty()) {
                customCoversMap[album.title] = album.coverPath
            }
        }
        Log.d(TAG, "# Сохранено ${customCoversMap.size} кастомных обложек альбомов ")

        // Очищаем альбомы
        albumDao.deleteAll()

        // Получаем все медиафайлы
        val mediaFiles = mediaDao.getAllFiles()

        // Группируем по альбомам
        val albumsMap = mediaFiles
            .groupBy { it.albumId to it.album }
            .mapValues { (key, songs) ->
                    val (albumId, albumName) = key
                    val artists = songs.map { it.artist ?: "Unknown" }.distinct()

                // ВОССТАНАВЛИВАЕМ КАСТОМНУЮ ОБЛОЖКУ
                val albumCoverPath = customCoversMap[albumName] ?: "" //todo

                AlbumFile(
                    albumId = albumId,
                    title = albumName ?: "Неизвестный альбом",
                    artist = if (artists.size > 1) "Разные исполнители" else artists.first(),
                    allArtists = artists.joinToString(";"),
                    songCount = songs.size,
                    coverPath = albumCoverPath
                )
            }

        Log.d(TAG, "# AlbumRepository Создано ${albumsMap.size} альбомов")

        try {
            // Сохраняем в базу
            albumDao.insertAll(albumsMap.values.toList())
            Log.d(TAG, "# AlbumRepository Альбомы успешно сохранены в базу")
        }catch (e: Exception){
            Log.d(TAG, "# AlbumRepository syncAlbumsFromMediaFiles Exception = ${e.message}")
        }
    }

    suspend fun getAllAlbumsWithSongs(): List<Album> {
        val albums = albumDao.getAllAlbums() //получаем список альбомов из базы
        return albums.map { albumFile ->
            val songFileList = mediaDao.getFilesByAlbumId(albumFile.albumId)
            //Log.d(TAG, "*AlbumRepository getAllAlbumsWithSongs songFileList size = ${songFileList.size}")
            Album(
                id =albumFile.id,
                title = albumFile.title,
                artist =albumFile.artist ,
                artists = albumFile.allArtists.toArtistList(),
                artworkUri =(albumFile.coverPath)?.toUri(),
                albumId = albumFile.albumId,
                songs = fromSongFileListToSongList (songFileList)
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
        return fromSongFileListToSongList(songFileList)
    }

    suspend fun updateAlbumArtUri(albumId: Long, artUri: String?) {
        albumDao.updateAlbumArtUri(albumId, artUri)
    }

    suspend fun getAlbumById(albumId:Long): AlbumFile?{
        return albumDao.getAlbumById(albumId)
    }
}