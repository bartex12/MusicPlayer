package com.example.muzpleer.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.muzpleer.room.entity.SongFile

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(file: SongFile)

    // Вставка списка элементов
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<SongFile>)

    @Update
    suspend fun update(file: SongFile)

    @Update
    suspend fun updateAll(files: List<SongFile>)

    @Delete
    suspend fun delete(file: SongFile)

    @Query("SELECT * FROM media_files WHERE mediaStoreId = :id LIMIT 1")
    suspend fun getById(id: Long): SongFile?

    @Query("SELECT mediaStoreId FROM media_files")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM media_files")
    suspend fun getAllFiles(): List<SongFile>

    @Query("DELETE FROM media_files WHERE mediaStoreId = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM media_files WHERE albumId = :albumId ORDER BY title")
    suspend fun getFilesByAlbumId(albumId: Long): List<SongFile>

    @Query("SELECT DISTINCT albumId FROM media_files WHERE albumId IS NOT NULL")
    suspend fun getAllAlbumIds(): List<Long>
}