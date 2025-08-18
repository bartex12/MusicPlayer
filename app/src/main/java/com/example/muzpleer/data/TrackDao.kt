package com.example.muzpleer.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackDao {

    // Вставляем список треков. Если трек уже есть (тот же filePath), он будет заменен
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    // Получаем все треки, отсортированные по названию
    @Query("SELECT * FROM tracks_table ORDER BY trackName ASC")
    suspend fun getAllTracks(): List<TrackEntity>

    // Полностью очищаем таблицу. Полезно для будущего обновления списка
    @Query("DELETE FROM tracks_table")
    suspend fun clearAllTracks()
}