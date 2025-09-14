package com.example.muzpleer.repository

import androidx.core.net.toUri
import com.example.muzpleer.model.Playlist
import com.example.muzpleer.model.Song
import com.example.muzpleer.room.dao.PlaylistDao
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.entity.PlaylistFile
import com.example.muzpleer.room.entity.PlaylistSongCrossRef
import com.example.muzpleer.room.utils.toPlaylist

class PlaylistRepository(
    private val playlistDao: PlaylistDao,
    private val songDao: SongDao
) {

    suspend fun getAllPlaylists(): List<Playlist> {
        return playlistDao.getAllPlaylists().map { it.toPlaylist(getSongsForPlaylist(it.id)) }
    }

    suspend fun getPlaylistWithSongs(playlistId: Long): Playlist? {
        val playlistFile = playlistDao.getPlaylistById(playlistId) ?: return null
        val songs = getSongsForPlaylist(playlistId)
        return playlistFile.toPlaylist(songs)
    }

    private suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        val crossRefs = playlistDao.getPlaylistSongs(playlistId)
        return crossRefs.mapNotNull { crossRef ->
            songDao.getById(crossRef.songId)?.toSong()
        }
    }

    suspend fun createPlaylist(name: String): Long {
        val playlistFile = PlaylistFile(
            playlistName = name,
            songCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        return playlistDao.insert(playlistFile)
    }

    suspend fun addSongsToPlaylist(playlistId: Long, songIds: List<Long>) {
        val currentMaxOrder = playlistDao.getMaxSongOrderInPlaylist(playlistId) ?: -1

        songIds.forEachIndexed { index, songId ->
            val crossRef = PlaylistSongCrossRef(
                playlistId = playlistId,
                songId = songId,
                sortOrder = currentMaxOrder + index + 1,
                addedAt = System.currentTimeMillis()
            )
            playlistDao.insertSong(crossRef)
        }

        // Обновляем счетчик песен
        updateSongCount(playlistId)
    }

    suspend fun updatePlaylistOrder(playlists: List<Playlist>) {
        playlists.forEachIndexed { index, playlist ->
            playlistDao.updatePlaylistOrder(playlist.id, index)
        }
    }

    private suspend fun updateSongCount(playlistId: Long) {
        val count = playlistDao.getSongCount(playlistId)
        val playlist = playlistDao.getPlaylistById(playlistId)
        playlist?.let {
            playlistDao.update(it.copy(songCount = count))
        }
    }

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deleteById(playlistId)
    }
}

