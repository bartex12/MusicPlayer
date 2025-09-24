package com.example.muzpleer.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.muzpleer.room.dao.AlbumDao
import com.example.muzpleer.room.dao.ArtistDao
import com.example.muzpleer.room.dao.FavoriteDao
import com.example.muzpleer.room.dao.FolderDao
import com.example.muzpleer.room.dao.PlaylistDao
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.entity.AlbumFile
import com.example.muzpleer.room.entity.ArtistFile
import com.example.muzpleer.room.entity.FavoriteSong
import com.example.muzpleer.room.entity.FolderFile
import com.example.muzpleer.room.entity.PlaylistFile
import com.example.muzpleer.room.entity.PlaylistSongCrossRef
import com.example.muzpleer.room.entity.SongFile

@Database(
    entities = [SongFile::class, AlbumFile::class, ArtistFile::class,
        FolderFile::class, FavoriteSong::class, PlaylistFile::class, PlaylistSongCrossRef::class],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun folderDao(): FolderDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val TAG = "33333"
        private const val DATABASE_NAME = "music_player.db"
//        val MIGRATION_3_5 = object : Migration(3, 5) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                Log.d("DatabaseMigration", "Starting migration from version 2 to 3")
//
//                try {
//                    // Создаем таблицу плейлистов
//                    database.execSQL("""
//                CREATE TABLE playlists (
//                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                    playlistName TEXT NOT NULL,
//                    playlistArtUri TEXT,
//                    songCount INTEGER NOT NULL DEFAULT 0,
//                    createdAt INTEGER NOT NULL,
//                    updatedAt INTEGER NOT NULL,
//                    sortOrder INTEGER NOT NULL DEFAULT 0
//                )
//            """.trimIndent())
//
//                    // Создаем таблицу связи плейлистов и песен
//                    database.execSQL("""
//                CREATE TABLE playlist_songs (
//                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
//                    playlistId INTEGER NOT NULL,
//                    songId INTEGER NOT NULL,
//                    sortOrder INTEGER NOT NULL DEFAULT 0,
//                    addedAt INTEGER NOT NULL,
//                    FOREIGN KEY(playlistId) REFERENCES playlists(id) ON DELETE CASCADE,
//                    FOREIGN KEY(songId) REFERENCES media_files(mediaStoreId) ON DELETE CASCADE
//                )
//            """.trimIndent())
//
//                    // Создаем индексы для производительности
//                    database.execSQL("CREATE INDEX index_playlists_sortOrder ON playlists(sortOrder)")
//                    database.execSQL("CREATE INDEX index_playlist_songs_playlistId ON playlist_songs(playlistId)")
//                    database.execSQL("CREATE INDEX index_playlist_songs_songId ON playlist_songs(songId)")
//                    database.execSQL("CREATE INDEX index_playlist_songs_sortOrder ON playlist_songs(sortOrder)")
//
//                    Log.d("DatabaseMigration", "Playlists migration completed successfully")
//
//                } catch (e: Exception) {
//                    Log.e("DatabaseMigration", "Playlists migration failed: ${e.message}")
//                    throw e
//                }
//            }
//        }

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
//                .addMigrations(MIGRATION_3_5)
                .fallbackToDestructiveMigration() // Удаляет и recreates при изменении версии
                .build()
        }
    }
}