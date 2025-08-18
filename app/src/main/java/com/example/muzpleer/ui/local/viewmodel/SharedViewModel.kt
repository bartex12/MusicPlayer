package com.example.muzpleer.ui.local.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Folder
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.repository.MusicRepository
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.ui.local.helper.IPreferenceHelper
import com.example.muzpleer.util.getSortedDataSong
import kotlinx.coroutines.launch
import org.koin.core.definition._createDefinition
import java.util.Locale
import kotlin.collections.find
import kotlin.let
import kotlin.ranges.coerceAtLeast

class SharedViewModel(
    var helper : IPreferenceHelper,
    private val repository: MusicRepository,
    private val playerHandler: MusicServiceHandler
) : ViewModel(), MusicServiceHandler.PlayerCallback{

    init {
        playerHandler.callback = this  //иначе не работает
        Log.d(TAG, "PlayerViewModel init: playerHandler =$playerHandler ")
    }


    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _filteredSongs = MutableLiveData<List<Song>>()
    val filteredSongs: LiveData<List<Song>> = _filteredSongs

    private val _playlist = MutableLiveData<List<Song>>()
    val playlist: LiveData<List<Song>> = _playlist

    private val _songAndPlaylist = MutableLiveData<SongAndPlaylist>()
    val songAndPlaylist: LiveData<SongAndPlaylist> = _songAndPlaylist

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> = _albums

    private val _filteredAlbums = MutableLiveData<List<Album>>()
    val filteredAlbums: LiveData<List<Album>> = _filteredAlbums

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> = _artists

    private val _filteredArtists = MutableLiveData<List<Artist>>()
    val filteredArtists: LiveData<List<Artist>> = _filteredArtists

    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders

    private val _filteredFolders = MutableLiveData<List<Folder>>()
    val filteredFolders: LiveData<List<Folder>> = _filteredFolders

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData<Long>(0L)
    val currentPosition: LiveData<Long> = _currentPosition

    private val _duration = MutableLiveData<Long>(0L)
    val duration: LiveData<Long> = _duration

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    //выделенная строка в адаптере песни при щелчке но ней
    private val _selectedSongPosition = MutableLiveData<Int>(RecyclerView.NO_POSITION)
    val selectedSongPosition: LiveData<Int> = _selectedSongPosition

    private val _selectedAlbumPosition = MutableLiveData<Int>(RecyclerView.NO_POSITION)
    val selectedAlbumPosition: LiveData<Int> = _selectedAlbumPosition

    private val _selectedArtistPosition = MutableLiveData<Int>(RecyclerView.NO_POSITION)
    val selectedArtistPosition: LiveData<Int> = _selectedArtistPosition

    private val _selectedFolderPosition = MutableLiveData<Int>(RecyclerView.NO_POSITION)
    val selectedFolderPosition: LiveData<Int> = _selectedFolderPosition

    private val _playerVisibility = MutableLiveData<Boolean>(true)
    val playerVisibility: LiveData<Boolean> = _playerVisibility

    //индекс выбранной песни в отсортированном(!) списке песен
    private val _indexOfCurrentSong = MutableLiveData<Int>(-1)
    val indexOfCurrentSong: LiveData<Int> = _indexOfCurrentSong


    init {
        Log.d(TAG, "SharedViewModel init ")
        viewModelScope.launch {
            _songs.value = repository.loadMusic()
            _filteredSongs.value  = _songs.value

            _albums.value = repository.getAlbums()
            _filteredAlbums.value = _albums.value

            _artists.value = repository.getArtists()
            _filteredArtists.value = _artists.value

            _folders.value = repository.getFolders()
            _filteredFolders.value = _folders.value
        }
    }

    fun setPlayerVisibility(visible: Boolean) {
        _playerVisibility.value = visible
    }

    fun getSongsByAlbum(albumId: String): LiveData<List<Song>> {
        return liveData {
            emit(repository.getAlbums().find { it.id == albumId }?.songs ?: emptyList())
        }
    }

    fun getSongsByArtist(artistId: String): LiveData<List<Song>> {
        return liveData {
            emit(repository.getArtists().find { it.id == artistId }?.songs ?: emptyList())
        }
    }

    fun getSongsByFolder(folderPath: String): LiveData<List<Song>> {
        return liveData {
            emit(repository.getFolders().find { it.path == folderPath }?.songs ?: emptyList())
        }
    }

    fun setSongAndPlaylist(songAndPlaylist: SongAndPlaylist){
        _songAndPlaylist.value = songAndPlaylist
    }

    fun setPlaylistForHandler(playlist: List<Song>, initialIndex: Int = 0) {
        playerHandler.setPlaylist(playlist, initialIndex)
    }

    fun setPlaylist(songsList : List<Song>){
        _playlist.value = songsList
    }

    fun togglePlayPause() {
        playerHandler.togglePlayPause()
    }

    fun playNext() {
        Log.d(TAG, "SharedViewModel playNext ")
        playerHandler.playNext()
    }

    fun playPrevious() {
        playerHandler.playPrevious()
    }

    fun seekTo(position: Long) {
        playerHandler.seekTo(position)
    }

    fun seekRelative(offsetMs: Long) {
        playerHandler.getCurrentPosition().let { currentPos ->
            val newPosition = (currentPos + offsetMs).coerceAtLeast(0)
            playerHandler.seekTo(newPosition)
        }
    }

    override fun onTrackChanged(track: Song) {
        _currentSong.value = track
        //считаем индекс выбранной песни в отсортированном списке песен,
        // чтобы при возврате на песни можно было перейти к этой песне по индексу
        val indexOfSong = getSortedDataSong(getSongs()).indexOfFirst { it.mediaUri == track.mediaUri }
        _indexOfCurrentSong.value = indexOfSong
        Log.d(TAG, "SharedViewModel onTrackChanged currentTrack = ${getCurrentSong()?.title}" +
                "   indexOfSong = $indexOfSong")
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }

    override fun onPositionChanged(position: Long, duration: Long) {
        _currentPosition.postValue(position)
        _duration.postValue(duration)
    }

    override fun onError(message: String) {
        _errorMessage.postValue(message)
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun setCurrentSong(song: Song) {
        Log.d(TAG, "SharedViewModel setCurrentSong song = $song")
        _currentSong.value = song
    }

    fun getCurrentSong() : Song?{
        //Log.d(TAG, "SharedViewModel getCurrentSong ")
         return currentSong.value
    }

    companion object{
        const val TAG= "33333"
    }

    override fun onCleared() {
        super.onCleared()
        playerHandler.release()
    }

    internal fun filterSongs(query: String) {
        val originalSongsList: MutableList<Song> =  (songs.value ?: listOf()).toMutableList()
        val filteredSongsList: MutableList<Song> = (filteredSongs.value ?: listOf()).toMutableList()
        filteredSongsList.clear()
        if (query.isEmpty()) {
            filteredSongsList.addAll(originalSongsList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            for (song in originalSongsList) {
                if (song.title.lowercase(Locale.getDefault()).contains(searchQuery) ||
                    song.artist.lowercase(Locale.getDefault()).contains(searchQuery)) {
                    filteredSongsList.add(song)
                }
            }
        }
        _filteredSongs.value = filteredSongsList
    }

    internal fun filterAlbums(query: String) {
        val originalAlbumList: MutableList<Album> =  (albums.value ?: listOf()).toMutableList()
        val filteredAlbumList: MutableList<Album> = (filteredAlbums.value ?: listOf()).toMutableList()
        filteredAlbumList.clear()
        if (query.isEmpty()) {
            filteredAlbumList.addAll(originalAlbumList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            for (album in originalAlbumList) {
                if (album.title.lowercase(Locale.getDefault()).contains(searchQuery) ||
                    album.artist.lowercase(Locale.getDefault()).contains(searchQuery)) {
                    filteredAlbumList.add(album)
                }
            }
        }
        _filteredAlbums.value = filteredAlbumList
    }
    internal fun filterArtists(query: String) {
        val originalArtistList: MutableList<Artist> =  (artists.value ?: listOf()).toMutableList()
        val filteredArtistList: MutableList<Artist> = (filteredArtists.value ?: listOf()).toMutableList()
        filteredArtistList.clear()
        if (query.isEmpty()) {
            filteredArtistList.addAll(originalArtistList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            for (artist in originalArtistList) {
                if (artist.name.lowercase(Locale.getDefault()).contains(searchQuery)) {
                    filteredArtistList.add(artist)
                }
            }
        }
        _filteredArtists.value = filteredArtistList
    }

    internal fun filterFolders(query: String) {
        val originalFolderList: MutableList<Folder> =  (folders.value ?: listOf()).toMutableList()
        val filteredFolderList: MutableList<Folder> = (filteredFolders.value ?: listOf()).toMutableList()
        filteredFolderList.clear()
        if (query.isEmpty()) {
            filteredFolderList.addAll(originalFolderList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            for (folder in originalFolderList) {
                if (folder.name.lowercase(Locale.getDefault()).contains(searchQuery)) {
                    filteredFolderList.add(folder)
                }
            }
        }
        _filteredFolders.value = filteredFolderList
    }

    fun getSongs():List<Song> {
       return songs.value
    }

    fun getPlaylist():List<Song> {
        return playlist.value
    }

    fun getPositionSong(): Int{  return helper.getPositionSong() }
    fun savePositionSong(position: Int){helper.savePositionSong(position)}

    fun getPositionAlbum(): Int{  return helper.getPositionAlbum() }
    fun savePositionAlbum(position: Int){helper.savePositionAlbum(position)}

    fun getPositionArtist(): Int{  return helper.getPositionArtist() }
    fun savePositionArtist(position: Int){helper.savePositionArtist(position)}

    fun getPositionFolder(): Int{  return helper.getPositionFolder() }
    fun savePositionFolder(position: Int){helper.savePositionFolder(position)}

    fun getTabsLocalPosition():Int = helper.getTabsLocalPosition()
    fun saveTabsLocalPosition(currentItem: Int) = helper.saveTabsLocalPosition(currentItem)

    fun setSelectedPosition(position: Int) { _selectedSongPosition.value = position }
    fun  getSelectedPosition(): Int { return _selectedSongPosition.value }

    fun resetSelection() {_selectedSongPosition.value = RecyclerView.NO_POSITION}
    fun setSelectedAlbumPosition(position: Int) { _selectedAlbumPosition.value = position }
    fun setSelectedArtistPosition(position: Int) { _selectedArtistPosition.value = position }
    fun setSelectedFolderPosition(position: Int) { _selectedFolderPosition.value = position }

    fun setCurrentSongById(songId: Long) {
        songs.value?.find { it.id == songId }?.let { song ->
            _currentSong.value = song //передаём сохранённую песню из преференсис
            setSongAndPlaylist( SongAndPlaylist( //передаём плейлист и текущую песню
                song = song,  //текущая песня
                playlist =getSortedDataSong(getSongs()) //текущий плейлист
            ))
        }?: run{
            //todo найти песню в моих треках, для чего перенести всё в SharedViewModel
        }
    }
    fun getSongAndPlaylist(): SongAndPlaylist{
        return songAndPlaylist.value
    }

    fun getIndexOfCurrentSong():Int{
        return indexOfCurrentSong.value
    }
}