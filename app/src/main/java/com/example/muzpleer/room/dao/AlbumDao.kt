package com.example.muzpleer.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.muzpleer.room.entity.AlbumFile
import com.example.muzpleer.room.entity.SongFile

@Dao
interface AlbumDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(album: AlbumFile)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(albums: List<AlbumFile>)

    @Update
    suspend fun update(album: AlbumFile)

    @Query("SELECT * FROM albums ORDER BY title COLLATE NOCASE")
    suspend fun getAllAlbums(): List<AlbumFile>

    @Query("SELECT * FROM albums WHERE mediaStoreId = :mediaStoreId LIMIT 1")
    suspend fun getAlbumByMediaStoreId(mediaStoreId: Long): AlbumFile?

    @Query("SELECT * FROM albums WHERE id = :id LIMIT 1")
    suspend fun getAlbumById(id: Long): AlbumFile?

    @Query("DELETE FROM albums WHERE mediaStoreId = :mediaStoreId")
    suspend fun deleteByMediaStoreId(mediaStoreId: Long)

    @Query("DELETE FROM albums")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM albums")
    suspend fun getCount(): Int

//    @Query("SELECT * FROM media_files WHERE albumId = :albumId ORDER BY trackNumber, title")
//    suspend fun getFilesByAlbumId(albumId: Long): List<SongFile>

    @Query("SELECT DISTINCT albumId FROM media_files WHERE albumId IS NOT NULL")
    suspend fun getAllAlbumIds(): List<Long>
}