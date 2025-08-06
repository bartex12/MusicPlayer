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
import com.example.muzpleer.model.Song
import androidx.core.net.toUri
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Folder
import java.io.File
import java.io.FileOutputStream
import kotlin.collections.set


class MusicRepository(private val context: Context) {
    companion object{
        const val TAG = "33333"
    }

    private val songs = mutableListOf<Song>()
    private val albums = mutableMapOf<String, Album>()
    private val artists = mutableMapOf<String, Artist>()
    private val folders = mutableMapOf<String, Folder>()

    fun loadMusic(): List<Song> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scanMusicApi29Plus(context)
        } else {
            scanMusicLegacy(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun scanMusicApi29Plus(context: Context): List<Song> {
        val musicList = mutableListOf<Song>()
        val collection = MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        )

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
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
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Неизвестно"
                val artist = cursor.getString(artistColumn) ?: "Неизвестно"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(pathColumn)
                val album = cursor.getString(albumColumn) ?: "Неизвестно"
                val albumId = cursor.getLong(albumIdColumn)

                val folderPath = File(path).parent ?: ""

                musicList.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        album = album,
                        albumId = albumId,
                        duration = duration,
                        mediaUri = path,
                        isLocal = true,
                        artworkUri = getArtworkUri(context, albumId, path ),
                        folderPath = folderPath

                    )
                )
//                Log.d(TAG, "#%# MediaScanner scanMusicApi29Plus MusicTrack:" +
//                        " title = $title  artist = $artist  path = $path  " +
//                        "artworkUri = ${getArtworkUri(context, albumId, path ).toString()} " +
//                        " album = $album albumId = $albumId ")
            }
        }
//        musicList.sortWith(compareBy(
//            { !it.title.matches(Regex(".*[А-Яа-яЁё].*")) },
//            { it.title.lowercase() }
//        ))
        // Build albums, artists and folders
        buildCollections(musicList)
        Log.d(TAG, "#%# MediaScanner scanMusicApi29Plus musicList.size = ${musicList.size}")

        return musicList
    }

    private fun buildCollections(songs: List<Song>) {
        albums.clear()
        artists.clear()
        folders.clear()

//        songs.groupBy { "${it.album}|${it.artist}" }.forEach { (key, albumSongs) ->
//            val parts = key.split("|")
//            val albumName = parts[0]
//            val artistName = parts[1]
//
//            albums[key] = Album(
//                id = key,
//                title = albumName,
//                artist = artistName,
//                artworkUri = albumSongs.firstOrNull()?.artworkUri,
//                songs = albumSongs
//            )
//        }

        // Группировка по альбомам (без учета исполнителя)
        songs.groupBy { it.album }.forEach { (albumName, albumSongs) ->
            // Собираем всех исполнителей в альбоме
            val artistsInAlbum = albumSongs.map { it.artist }.distinct()

            // Создаем строку с перечислением исполнителей
            val artistsString = if (artistsInAlbum.size > 3) {
                "${artistsInAlbum.take(3).joinToString()} и ещё ${artistsInAlbum.size - 3}"
            } else {
                artistsInAlbum.joinToString()
            }

            albums[albumName.toString()] = Album(
                id = albumName.toString(),
                title = albumName.toString(),
                // Для отображения можно использовать "Various Artists" или список исполнителей
                artist = if (artistsInAlbum.size > 1) "Various Artists" else artistsInAlbum.first(),
                artists = artistsInAlbum, // Сохраняем всех исполнителей
                artworkUri = albumSongs.maxByOrNull { it.duration }?.artworkUri, // Берём обложку из самой длинной песни
                songs = albumSongs
            )
        }

        songs.groupBy { it.artist }.forEach { (artistName, artistSongs) ->
            //val artistArtUri = getArtworkUriFromMediaStore(artistSongs)
            artists[artistName] = Artist(
                id = artistName,
                name = artistName,
                songs = artistSongs,
                artworkUri = artistSongs.firstOrNull()?.artworkUri
            )
        }

        songs.groupBy { it.folderPath }.forEach { (folderPath, folderSongs) ->
            val folderName = File(folderPath).name
            folders[folderPath] = Folder(
                path = folderPath,
                name = folderName,
                songs = folderSongs,
                artworkUri = folderSongs.firstOrNull()?.artworkUri
            )
        }
    }

    fun getAlbums(): List<Album> = albums.values.toList()
    fun getArtists(): List<Artist> = artists.values.toList()
    fun getFolders(): List<Folder> = folders.values.toList()

    private fun scanMusicLegacy(context: Context): List<Song> {
        val musicList = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Неизвестно"
                val artist = cursor.getString(artistColumn) ?: "Неизвестно"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(pathColumn)
                val album = cursor.getString(albumColumn) ?: "Неизвестно"
                val albumId = cursor.getLong(albumIdColumn)

                val folderPath = File(path).parent ?: ""

                musicList.add(
                    Song(
                        id = id,
                        title = title,
                        artist = artist,
                        duration = duration,
                        mediaUri = path,
                        isLocal = true,
                        artworkUri = getArtworkUri(context, albumId, path ),
                        album = album,
                        albumId = albumId,
                        folderPath = folderPath
                    )
                )
                Log.d(TAG, "#%# MediaScanner scanMusicLegacy MusicTrack:" +
                        " title = $title  artist = $artist  path = $path  " +
                        "artworkUri = ${getArtworkUri(context, albumId, path ).toString()} " +
                        " album = $album albumId = $albumId ")
            }
        }
        musicList.sortWith(compareBy(
            { it.title.matches(Regex(".*[А-Яа-яЁё].*")) },
            { it.title.lowercase() }
        ))
        // Build albums, artists and folders
        buildCollections(musicList)

        Log.d(TAG, "#%# MediaScanner scanMusicLegacy musicList.size = ${musicList.size}")
        return musicList
    }

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