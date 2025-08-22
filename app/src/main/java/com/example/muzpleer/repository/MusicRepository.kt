package com.example.muzpleer.repository

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Folder
import com.example.muzpleer.model.Song
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.entity.SongFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class MusicRepository(
    private val songDao: SongDao,
    private val context: Context){

    companion object{
        const val TAG = "33333"
    }

    private var songs = mutableListOf<Song>()
    private val albums = mutableMapOf<String, Album>()
    private val artists = mutableMapOf<String, Artist>()
    private val folders = mutableMapOf<String, Folder>()

     suspend fun loadMusic() {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scanMusicApi29Plus(context)
        } else {
            //scanMusicLegacy(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun scanMusicApi29Plus(context: Context) {

        val dbIds = songDao.getAllIds().toMutableSet()
        val filesToAdd = mutableListOf<SongFile>()
        val filesToUpdate = mutableListOf<SongFile>()
        val filesToDelete = mutableListOf<SongFile>()

        //val musicList = mutableListOf<Song>()

        val collection = MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        )

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_MODIFIED,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val modifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
            val addedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val path = cursor.getString(pathColumn)
                val lastModified = cursor.getLong(modifiedColumn) * 1000 // конвертируем в миллисекунды
                val title = cursor.getString(titleColumn) ?: "Неизвестно"
                val artist = cursor.getString(artistColumn) ?: "Неизвестно"
                val album = cursor.getString(albumColumn) ?: "Неизвестно"
                val albumId = cursor.getLong(albumIdColumn)
                val duration = cursor.getLong(durationColumn)
                val sizeFile = cursor.getLong(sizeColumn)
                val dateAdded = cursor.getLong(addedColumn) * 1000
                val folderPath = File(path).parent ?: ""

                songDao.getById(id)?.let { existingFile ->
                    // Файл существует в базе
                    dbIds.remove(id)
                    if (existingFile.lastModified != lastModified) {
                        filesToUpdate.add(
                            SongFile(
                                mediaStoreId = id,
                                path = path,
                                lastModified = lastModified,
                                title = title,
                                artist = artist,
                                album = album,
                                albumId = albumId,
                                duration = duration,
                                isLocal = true,
                                size = sizeFile,
                                dateAdded = dateAdded,
                                folderPath = folderPath,
                                artUri = null
                            )
                        )
                    }
                } ?: run {
                    // Новый файл
                    filesToAdd.add(
                        SongFile(
                            mediaStoreId = id,
                            path = path,
                            lastModified = lastModified,
                            title = title,
                            artist = artist,
                            album = album,
                            albumId = albumId,
                            duration = duration,
                            isLocal = true,
                            size = sizeFile,
                            dateAdded = dateAdded,
                            folderPath = folderPath,
                            artUri = null
                        )
                    )
                }
//                Log.d(TAG, "#%# MediaScanner scanMusicApi29Plus MusicTrack:" +
//                        " title = $title  artist = $artist  path = $path  " +
//                        "artworkUri = ${getArtworkUri(context, albumId, path ).toString()} " +
//                        " album = $album albumId = $albumId ")


                // Удаляем файлы, которые есть в базе, но нет в MediaStore
                dbIds.forEach { id ->
                    songDao.getById(id)?.let { filesToDelete.add(it) }
                }

                // Применяем изменения
                withContext(Dispatchers.IO) {
                    // Вставляем новые файлы списком
                    if (filesToAdd.isNotEmpty()) {
                        songDao.insertAll(filesToAdd)
                    }

                    // Обновляем существующие файлы списком
                    if (filesToUpdate.isNotEmpty()) {
                        songDao.updateAll(filesToUpdate)
                    }

                }
            }
        }
        // Build albums, artists and folders
        buildCollections()
        //Log.d(TAG, "#%# MediaScanner scanMusicApi29Plus musicList.size = ${musicList.size}")

    }

    private suspend fun buildCollections() {
        albums.clear()
        artists.clear()
        folders.clear()

        val songList = songDao.getAllFiles().map{songFile->
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

        songs = songList.toMutableList()
        Log.d(TAG, "#%# MediaScanner buildCollections songList.size = ${songList.size}")

        //Группировка по albumId и названию - избегаем дублирования альбомов
        songList.groupBy { it.albumId to it.album } // Группируем по albumId и названию
            .forEach { (albumKey, albumSongs) ->
                val (albumId, albumName) = albumKey
                // Собираем всех исполнителей в альбоме
                val artistsInAlbum = albumSongs.map { it.artist }.distinct()
                // Находим песню с обложкой (если есть) или первую песню
                //val artworkSong = albumSongs.firstOrNull { it.artworkUri != null } ?: albumSongs.firstOrNull()

                albums[albumId.toString()] = Album(
                    id = albumId.toString(),
                    title = albumName.toString(),
                    artist = if (artistsInAlbum.size > 1) "Разные исполнители" else artistsInAlbum.first(),
                    artists = artistsInAlbum,
                    //artworkUri = artworkSong?.artworkUri,
                    albumId = albumId, // Сохраняем albumId для последующей загрузки обложки
                    songs = albumSongs
                )
            }

        // группировка по исполнителям с добавлением списка альбомов
        // в альбомы каждого исполнителя добавлено поле artists со списком исполнителей
        songList.groupBy { it.artist }.forEach { (artistName, artistSongs) ->
            //val artistArtUri = getArtworkUriFromMediaStore(artistSongs)
            // Получаем уникальные альбомы исполнителя
            val artistAlbums =
                artistSongs.groupBy {  it.albumId to it.album} // Группируем песни по альбомам
                            .mapValues { (albumKey, albumSongs) ->
                                val (albumId, albumName) = albumKey
                                val artists = albumSongs.map { it.artist }.distinct()
                          Album(
                              id = albumId.toString(),
                              title =albumName.toString(),
                              artist = if (artists.size > 1) "Разные исполнители" else artists.first(),
                              songs = albumSongs,
                              artists = artists,
                              albumId = albumId,
                             // artworkUri = albumSongs.firstOrNull { it.artworkUri != null }?.artworkUri
                          )
                 }.map {it.value}

            artists[artistName] = Artist(
                id = artistName,
                name = artistName,
                songs = artistSongs,
                //artworkUri = artistSongs.firstOrNull()?.artworkUri,
                albums = artistAlbums // Добавляем список альбомов
            )
        }

        songList.groupBy { it.folderPath }.forEach { (folderPath, folderSongs) ->
            val folderName = File(folderPath).name
            folders[folderPath] = Folder(
                path = folderPath,
                name = folderName,
                songs = folderSongs,
               // artworkUri = folderSongs.firstOrNull()?.artworkUri
            )
        }
    }

    fun getSongs(): List<Song> = songs.toList()
    fun getAlbums(): List<Album> = albums.values.toList()
    fun getArtists(): List<Artist> = artists.values.toList()
    fun getFolders(): List<Folder> = folders.values.toList()

//    private fun scanMusicLegacy(context: Context){
//        val musicList = mutableListOf<Song>()
//        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
//        val projection = arrayOf(
//            MediaStore.Audio.Media._ID,
//            MediaStore.Audio.Media.TITLE,
//            MediaStore.Audio.Media.ARTIST,
//            MediaStore.Audio.Media.DURATION,
//            MediaStore.Audio.Media.DATA,
//            MediaStore.Audio.Media.ALBUM,
//            MediaStore.Audio.Media.ALBUM_ID
//        )
//
//        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
//        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"
//
//        context.contentResolver.query(
//            uri,
//            projection,
//            selection,
//            null,
//            sortOrder
//        )?.use { cursor ->
//            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
//            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
//            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
//            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
//            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
//            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
//            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
//
//            while (cursor.moveToNext()) {
//                val id = cursor.getLong(idColumn)
//                val title = cursor.getString(titleColumn) ?: "Неизвестно"
//                val artist = cursor.getString(artistColumn) ?: "Неизвестно"
//                val duration = cursor.getLong(durationColumn)
//                val path = cursor.getString(pathColumn)
//                val album = cursor.getString(albumColumn) ?: "Неизвестно"
//                val albumId = cursor.getLong(albumIdColumn)
//
//                val folderPath = File(path).parent ?: ""
//
//                musicList.add(
//                    Song(
//                        id = id,
//                        title = title,
//                        artist = artist,
//                        duration = duration,
//                        mediaUri = path,
//                        isLocal = true,
//                        //artworkUri = getArtworkUri(context, albumId, path ),
//                        album = album,
//                        albumId = albumId,
//                        folderPath = folderPath
//                    )
//                )
//                Log.d(TAG, "#%# MediaScanner scanMusicLegacy MusicTrack:" +
//                        " title = $title  artist = $artist  path = $path  " +
//                        "artworkUri = ${getArtworkUri(context, albumId, path ).toString()} " +
//                        " album = $album albumId = $albumId ")
//            }
//        }
//        musicList.sortWith(compareBy(
//            { it.title.matches(Regex(".*[А-Яа-яЁё].*")) },
//            { it.title.lowercase() }
//        ))
//        // Build albums, artists and folders
//        buildCollections()
//
//        Log.d(TAG, "#%# MediaScanner scanMusicLegacy musicList.size = ${musicList.size}")
//        return musicList
//    }

    private var cachedArtworkUri: Uri? = null

    fun getArtworkUri(context: Context, albumId:Long, mediaUri:String): Uri {
        return cachedArtworkUri ?: run {
            val uri = when {
                albumId != -1L -> getArtworkUriFromMediaStore(albumId)
                else -> tryExtractFromFile(context, mediaUri)
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

    private fun tryExtractFromFile(context: Context, mediaUri: String): Uri {
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
        return "android.resource://${context.packageName}/drawable/gimme.png".toUri()
    }

}