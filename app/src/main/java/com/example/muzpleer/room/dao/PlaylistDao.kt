package com.example.muzpleer.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.muzpleer.room.entity.PlaylistFile
import com.example.muzpleer.room.entity.PlaylistSongCrossRef

@Dao
interface PlaylistDao {
    // Основные операции с плейлистами
    @Insert
    suspend fun insert(playlist: PlaylistFile): Long

    @Update
    suspend fun update(playlist: PlaylistFile)

    @Delete
    suspend fun delete(playlist: PlaylistFile)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deleteById(playlistId: Long)

    @Query("SELECT * FROM playlists ORDER BY sortOrder ASC, createdAt DESC")
    suspend fun getAllPlaylists(): List<PlaylistFile>

    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    suspend fun getPlaylistById(playlistId: Long): PlaylistFile?

    // Операции с песнями в плейлистах
    @Insert
    suspend fun insertSong(crossRef: PlaylistSongCrossRef)

    @Delete
    suspend fun deleteSong(crossRef: PlaylistSongCrossRef)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun removeSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun clearPlaylist(playlistId: Long)

    @Query("SELECT * FROM playlist_songs WHERE playlistId = :playlistId ORDER BY sortOrder ASC")
    suspend fun getPlaylistSongs(playlistId: Long): List<PlaylistSongCrossRef>

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getSongCount(playlistId: Long): Int

    @Update
    suspend fun updateSongOrder(crossRef: PlaylistSongCrossRef)

    @Query("UPDATE playlist_songs SET sortOrder = :newOrder WHERE playlistId = :playlistId AND songId = :songId")
    suspend fun updateSongSortOrder(playlistId: Long, songId: Long, newOrder: Int)

    // Обновление порядка плейлистов
    @Query("UPDATE playlists SET sortOrder = :newOrder WHERE id = :playlistId")
    suspend fun updatePlaylistOrder(playlistId: Long, newOrder: Int)

    @Query("SELECT MAX(sortOrder) FROM playlists")
    suspend fun getMaxPlaylistOrder(): Int?

    @Query("SELECT MAX(sortOrder) FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun getMaxSongOrderInPlaylist(playlistId: Long): Int?
}