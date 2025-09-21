package com.example.muzpleer.repository

import android.util.Log
import android.widget.Toast
import com.example.muzpleer.di.App
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

    companion object{
        const val TAG= "33333"
    }
    suspend fun getAllPlaylists(): List<Playlist> {
        val listOfPlaylists = playlistDao.getAllPlaylists().map { it.toPlaylist(getSongsForPlaylist(it.id)) }
        Log.d(TAG, "PlaylistRepository getAllPlaylists: listOfPlaylists size = ${listOfPlaylists.size}  ")
        return listOfPlaylists
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

    suspend fun addSongsToPlaylistWithSongs(playlistId: Long, songs: List<Song>) {
        // Получаем ID существующих песен в плейлисте
        val existingSongIds = playlistDao.getSongIdsInPlaylist(playlistId).toSet()

        // Фильтруем песни, оставляем только те, которых еще нет в плейлисте
        val newSongs = songs.filter { song ->
            !existingSongIds.contains(song.id)
        }

        if (newSongs.isEmpty()) {
            if(songs.size == 1){
                Toast.makeText(App.instance, "Эта песня уже есть в плейлисте", Toast.LENGTH_SHORT).show()
            }else {
                Toast.makeText(App.instance, "Эти песни уже есть в плейлисте", Toast.LENGTH_SHORT).show()
            }
            return
        }

        val songIds = newSongs.map { it.id }
        addSongsToPlaylist(playlistId, songIds)
        // Показать сообщение об успехе и вернуться к выбору источника
        val songsSelected = songs.size
        val songsAdded = songIds.size
        val songsWereInPlaylist = songs.size - songIds.size
        Toast.makeText(App.instance,
            "Выбрано: $songsSelected; Добавлено: $songsAdded; Уже было в плейлисте: $songsWereInPlaylist", Toast.LENGTH_LONG).show()
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

    suspend fun renamePlaylist(playlistId: Long, newName: String) {
        playlistDao.renamePlaylist(playlistId, newName)
    }

    suspend fun updatePlaylistArtUri(playlistId: Long, artUri: String?) {
        playlistDao.updatePlaylistArtUri(playlistId, artUri)
    }

}

