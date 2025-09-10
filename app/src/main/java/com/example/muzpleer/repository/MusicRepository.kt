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
import com.example.muzpleer.room.utils.fromSongFileListToSongList
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
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

     suspend fun loadMusic():List<Song> {
        return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scanMusicApi29Plus(context)
        } else {
            scanMusicLegacy(context)
        })
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun scanMusicApi29Plus(context: Context):List<Song> {

        val dbIds = songDao.getAllIds().toMutableSet() //список id в базе
        Log.d(TAG, "#%# MusicRepository scanMusicApi29Plus dbIds size = ${dbIds.size}")
        val filesToAdd = mutableListOf<SongFile>()  //список для добавления
        val filesToUpdate = mutableListOf<SongFile>() //список для обновления
        val filesToDelete = mutableListOf<SongFile>()   //список для удаления

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
                    dbIds.remove(id)  //удаляем запись с этой id из списка всех id

                    // Проверяем и дату изменения, и путь
                    val isModified = existingFile.lastModified != lastModified
                    val isMoved = existingFile.path != path

                    if (isModified || isMoved) {
                        filesToUpdate.add(
                            SongFile(
                                mediaStoreId = id,
                                path = path,
                                lastModified = lastModified,
                                title = title,
                                artist = artist,
                                artistId = artist.hashCode().toLong(),
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
                            artistId = artist.hashCode().toLong(),
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
            }
        }

        // файлы, которые есть в базе, но нет в MediaStore
        //берем список всех id и для каждого id пробуем получить запись в базе
        //если запись есть, помещаем ее в список для удаления, так как до этого
        //все записи просмотрели?
        dbIds.forEach { id ->
            songDao.getById(id)?.let {
                filesToDelete.add(it)
                Log.d(TAG, "#%# MusicRepository scanMusicApi29Plus filesToDelete size= ${filesToDelete.size}")
            }
        }

        Log.d(TAG, "#%# MusicRepository scanMusicApi29Plus " +
                "filesToAdd.size = ${filesToAdd.size}" +
                "filesToUpdate.size = ${filesToUpdate.size}" +
                "filesToDelete.size = ${filesToDelete.size}")
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
            // Удаляем файлы
            filesToDelete.forEach { songDao.delete(it) }
        }

        val songList = fromSongFileListToSongList(songDao.getAllFiles())
        Log.d(TAG, "#%# MusicRepository scanMusicApi29Plus songList.size = ${songList.size}")

        songs = songList.toMutableList()
        return songList
    }

    suspend fun getSongsFromDatabase(): List<Song> { //для проверки в MainActivity
        return fromSongFileListToSongList(songDao.getAllFiles())
    }

    private fun scanMusicLegacy(context: Context):List<Song>{
        return listOf()
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

    suspend fun updateCoverPath(id : Long, coverPath:String) {
        Log.d(TAG, "5*** MusicRepository updateCoverPath id = $id coverPath = $coverPath")
        songDao.updateCoverPath(id, coverPath)
        Log.d(TAG, "7***MusicRepository updateCoverPath  coverPath из базы = ${songDao.getById(id)?.artUri}")
    }

    suspend fun updateSongInfo(
        songId: Long,
        title: String,
        artist: String,
        album: String?,
        author: String?,
        genre: String?,
        year: Int?
    ) {
        Log.d(TAG, "8*** MusicRepository updateSongInfo title = $title author = $author")
        songDao.updateSongInfo(songId, title, artist, album.toString(), author, genre, year)
        Log.d(TAG, "9***MusicRepository updateSongInfo  author из базы = ${songDao.getById(songId)?.author}")
    }
}
