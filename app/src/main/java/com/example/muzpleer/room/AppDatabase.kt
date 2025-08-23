package com.example.muzpleer.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.muzpleer.room.dao.AlbumDao
import com.example.muzpleer.room.dao.ArtistDao
import com.example.muzpleer.room.dao.FolderDao
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.entity.AlbumFile
import com.example.muzpleer.room.entity.SongFile

@Database(
    entities = [SongFile::class, AlbumFile::class],
    //entities = [SongFile::class, FolderFile::class, AlbumFile::class, ArtistFile::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun folderDao(): FolderDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao

    companion object {
        private const val DATABASE_NAME = "music_player.db"

        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // Удаляет и recreates при изменении версии
                .build()
        }
    }
}