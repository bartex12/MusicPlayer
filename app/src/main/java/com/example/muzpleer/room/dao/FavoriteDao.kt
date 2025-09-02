package com.example.muzpleer.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.muzpleer.room.entity.FavoriteSong

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favorite: FavoriteSong)

    @Delete
    suspend fun delete(favorite: FavoriteSong)

    @Query("DELETE FROM favorite_songs WHERE songId = :songId")
    suspend fun deleteBySongId(songId: Long)

    @Query("SELECT * FROM favorite_songs ORDER BY addedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteSong>

    @Query("SELECT * FROM favorite_songs WHERE songId = :songId LIMIT 1")
    suspend fun getFavoriteBySongId(songId: Long): FavoriteSong?

    @Query("SELECT COUNT(*) FROM favorite_songs WHERE songId = :songId")
    suspend fun isFavorite(songId: Long): Boolean

    @Query("DELETE FROM favorite_songs")
    suspend fun deleteAll()
}