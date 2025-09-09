package com.example.muzpleer.repository

import android.util.Log
import androidx.core.net.toUri
import com.example.muzpleer.model.Folder
import com.example.muzpleer.model.Song
import com.example.muzpleer.room.dao.FolderDao
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.entity.FolderFile
import com.example.muzpleer.room.utils.fromSongFileListToSongList
import java.io.File

class FolderRepository (private val folderDao: FolderDao,
                        private val songDao: SongDao,
                       ){
    // Первый запуск - группировка и сохранение
    suspend fun syncFoldersFromMediaFiles() {
        Log.d(TAG, "# FolderRepository Начало синхронизации папок...")

        // ОЧИСТКА ПЕРЕД СИНХРОНИЗАЦИЕЙ!
        folderDao.deleteAll()

        val mediaFiles = songDao.getAllFiles()
        Log.d(TAG, "# FolderRepository syncFolderFromMediaFiles Получено ${mediaFiles.size} медиафайлов")

        // Группируем по папкам
        val foldersMap = mediaFiles
            .groupBy { it.folderPath ?: "Unknown" }
            .mapValues { (folderPath, songs) ->

                val folderName = getFolderNameFromPath(folderPath)

                FolderFile(
                    folderPath = folderPath,
                    folderName = folderName,
                    songCount = songs.size,
                    coverPath = songs.firstOrNull { it.artUri != null }?.artUri
                )
            }

        Log.d(TAG, "# FolderRepository syncFolderFromMediaFiles Создано ${foldersMap.size} папок")

        try {
            folderDao.insertAll(foldersMap.values.toList())
            Log.d(TAG, "# FolderRepository syncFolderFromMediaFiles  Папки успешно сохранены в базу")
            val folderSize = folderDao.getAllFolders().size
            Log.d(TAG, "# FolderRepository syncFolderFromMediaFiles из базы folderSize = $folderSize" )
        } catch (e: Exception) {
            Log.d(TAG, "# FolderRepository syncFolderFromMediaFiles Ошибка сохранения папок: ${e.message}")
        }
    }

    // Последующие запуски - получение из базы
    suspend fun getAllFoldersWithSongs(): List<Folder> {
        // Получаем все папки  из базы
        val folders = folderDao.getAllFolders()

        return folders.map { folder ->
            // Получаем все песни в папке
            val songFiles  = songDao.getFilesByFolderPath(folder.folderPath)
            val songs = fromSongFileListToSongList(songFiles)
            Folder(
                        path = folder.folderPath,
                        name = folder.folderName,
                        songs = songs,
                        artworkUri =folder.coverPath?.toUri(),
            )
        }
    }

//    suspend fun getFolderWithSongs(folderPath: String): Folder {
//        val folder = folderDao.getFolderByPath(folderPath) ?: throw Exception("Папка не найдена")
//        val songFiles = songDao.getFilesByFolderPath(folderPath)
//        val songs = fromSongFileListToSongList(songFiles)
//
//        return Folder(
//            path = folder.folderPath,
//            name = folder.folderName,
//            songs = songs,
//            artworkUri =folder.coverPath?.toUri(),
//        )
//    }

    private fun getFolderNameFromPath(path: String): String {
        return if (path.isBlank()) {
            "Unknown"
        } else {
            File(path).name
        }
    }

    suspend fun getFolderSongList(folderPath: String): List<Song> {
        //val folder = folderDao.getFolderByPath(folderPath) ?: throw Exception("Папка не найдена")
        val songFiles = songDao.getFilesByFolderPath(folderPath)
       return fromSongFileListToSongList(songFiles)
    }

    companion object{
        const val TAG = "33333"
    }
}