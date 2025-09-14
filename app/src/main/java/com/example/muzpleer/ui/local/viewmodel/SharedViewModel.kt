package com.example.muzpleer.ui.local.viewmodel

import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.example.muzpleer.di.App
import com.example.muzpleer.model.Album
import com.example.muzpleer.model.Artist
import com.example.muzpleer.model.Folder
import com.example.muzpleer.model.Playlist
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.SongAndPlaylist
import com.example.muzpleer.repository.AlbumRepository
import com.example.muzpleer.repository.ArtistRepository
import com.example.muzpleer.repository.FavoriteRepository
import com.example.muzpleer.repository.FolderRepository
import com.example.muzpleer.repository.MusicRepository
import com.example.muzpleer.repository.PlaylistRepository
import com.example.muzpleer.room.entity.FavoriteSong
import com.example.muzpleer.room.entity.SongFile
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.ui.local.helper.IPreferenceHelper
import com.example.muzpleer.util.getSortedDataSong
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Locale

class SharedViewModel(
    var helper : IPreferenceHelper,
    private val repository: MusicRepository,
    private val albumRepository: AlbumRepository,
    private val artistsRepository: ArtistRepository,
    private val playerHandler: MusicServiceHandler,
    private val folderRepository: FolderRepository,
    private val favoriteRepository: FavoriteRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel(), MusicServiceHandler.PlayerCallback{

    init {
        playerHandler.callback = this  //иначе не работает
        Log.d(TAG, "PlayerViewModel init: playerHandler =$playerHandler ")
    }


    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> = _songs

    private val _filteredSongs = MutableLiveData<List<Song>>()
    val filteredSongs: LiveData<List<Song>> = _filteredSongs

    private val _favoriteSongs = MutableLiveData<List<Song>>(listOf())
    val favoriteSongs: LiveData<List<Song>> = _favoriteSongs

    private var _filteredFavoriteSongs = MutableLiveData<List<Song>>()
    val filteredFavoriteSongs: LiveData<List<Song>> = _filteredFavoriteSongs

    private val _playlist = MutableLiveData<List<Song>>()
    val playlist: LiveData<List<Song>> = _playlist

    private val _songAndPlaylist = MutableLiveData<SongAndPlaylist>()
    val songAndPlaylist: LiveData<SongAndPlaylist> = _songAndPlaylist

    private val _albums = MutableLiveData<List<Album>>()
    val albums: LiveData<List<Album>> = _albums

    private val _filteredAlbums = MutableLiveData<List<Album>>()
    val filteredAlbums: LiveData<List<Album>> = _filteredAlbums

    private val _listAlbumSong = MutableLiveData<List<Song>>()
    val listAlbumSong: LiveData<List<Song>> = _listAlbumSong

    private val _filteredListAlbumSong = MutableLiveData<List<Song>>()
    val filteredListAlbumSong: LiveData<List<Song>> = _filteredListAlbumSong

    private val _artists = MutableLiveData<List<Artist>>()
    val artists: LiveData<List<Artist>> = _artists

    private val _filteredArtists = MutableLiveData<List<Artist>>()
    val filteredArtists: LiveData<List<Artist>> = _filteredArtists

    private val _listArtistSong = MutableLiveData<List<Song>>()
    val listArtistSong: LiveData<List<Song>> = _listArtistSong

    private val _filteredListArtistSong = MutableLiveData<List<Song>>()
    val filteredListArtistSong: LiveData<List<Song>> = _filteredListArtistSong

    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders

    private val _filteredFolders = MutableLiveData<List<Folder>>()
    val filteredFolders: LiveData<List<Folder>> = _filteredFolders

    private val _listFolderSong = MutableLiveData<List<Song>>()
    val listFolderSong: LiveData<List<Song>> = _listFolderSong

    private val _filteredListFolderSong = MutableLiveData<List<Song>>()
    val filteredListFolderSong: LiveData<List<Song>> = _filteredListFolderSong

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> = _currentSong

    private val _coverImage = MutableLiveData<Bitmap?>()
    val coverImage: LiveData<Bitmap?> = _coverImage

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

    private val _selectedSong = MutableLiveData<Song?>()
    val selectedSong: LiveData<Song?> = _selectedSong

    private val _coverImageUri = MutableLiveData<Uri?>()
    val coverImageUri: LiveData<Uri?> = _coverImageUri

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _coverPath = MutableLiveData<String>()
    val coverPath: LiveData<String> = _coverPath

    private val _playlists = MutableLiveData<List<Playlist>>()
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _filteredPlaylists = MutableLiveData<List<Playlist>>()
    val filteredPlaylists: LiveData<List<Playlist>> = _filteredPlaylists

    private val _currentPlaylist = MutableLiveData<Playlist?>()
    val currentPlaylist: LiveData<Playlist?> = _currentPlaylist

     fun scanMedia(afterLoad:()->Unit) {
        viewModelScope.launch {
            initParamsSong(repository.loadMusic())
            afterLoad.invoke()
        }
    }

    fun getRepositorySong(listSong:(List<Song>)->Unit ) {
        viewModelScope.launch {
        val list =  repository.getSongsFromDatabase()
            listSong.invoke(list)
        }
    }

//    fun getAllMediaFiles(songs:List<Song>){  //берём список песен из базы
//        viewModelScope.launch {
//            repository.buildCollections(songs)
//            initParams(songs)
//        }
//    }

    private fun initParamsSong(songs:List<Song>){
        _songs.value = songs
        _filteredSongs.value  = _songs.value
        Log.d(TAG, "SharedViewModel initParamsSong songs.size = ${songs.size}")
    }

    fun setPlayerVisibility(visible: Boolean) {
        _playerVisibility.value = visible
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
        //так не меняется автоматом- остаётся старый artUri из плейлиста
        _currentSong.value = track
        //считаем индекс выбранной песни в отсортированном списке песен,
        // чтобы при возврате на песни можно было перейти к этой песне по индексу
        val indexOfSong = getSortedDataSong(getSongs()).indexOfFirst { it.mediaUri == track.mediaUri }
        _indexOfCurrentSong.value = indexOfSong
        Log.d(TAG, " !@# !@# SharedViewModel onTrackChanged currentTrack = ${getCurrentSong()?.title}" +
                "   indexOfSong = $indexOfSong  TRACK.artUri = ${track.artUri}")
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

    fun getFavoriteSongs():List<Song> {
        return favoriteSongs.value
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

    internal fun filterPlaylists(query: String) {
        val originalPlaylistList: MutableList<Playlist> =  (playlists.value ?: listOf()).toMutableList()
        val filteredPlaylistList: MutableList<Playlist> = (filteredPlaylists.value ?: listOf()).toMutableList()
        filteredPlaylistList.clear()
        if (query.isEmpty()) {
            filteredPlaylistList.addAll(originalPlaylistList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            for (playlist in originalPlaylistList) {
                if (playlist.playlistName.lowercase(Locale.getDefault()).contains(searchQuery) ) {
                    filteredPlaylistList.add(playlist)
                }
            }
        }
        _filteredPlaylists.value = filteredPlaylistList
    }

    internal fun filterAlbumSongs(query: String) {
        val originalAlbumSongList = (listAlbumSong.value?: listOf()).toMutableList()
        val filteredAlbumSongList: MutableList<Song> = (filteredListAlbumSong.value ?: listOf()).toMutableList()
        filteredAlbumSongList.clear()
        if (query.isEmpty()) {
            filteredAlbumSongList.addAll(originalAlbumSongList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            for (albumSong in originalAlbumSongList) {
                if (albumSong.title.lowercase(Locale.getDefault()).contains(searchQuery) ||
                    albumSong.artist.lowercase(Locale.getDefault()).contains(searchQuery)) {
                    filteredAlbumSongList.add(albumSong)
                }
            }
        }
        _filteredListAlbumSong.value = filteredAlbumSongList
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

    internal fun filterArtistSongs(query: String) {
        val originalArtistSongList = (listArtistSong.value?: listOf()).toMutableList()
        val filteredArtistSongList: MutableList<Song> = (filteredListArtistSong.value ?: listOf()).toMutableList()
        filteredArtistSongList.clear()
        if (query.isEmpty()) {
            filteredArtistSongList.addAll(originalArtistSongList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            for (artistSong in originalArtistSongList) {
                if (artistSong.title.lowercase(Locale.getDefault()).contains(searchQuery) ||
                    artistSong.artist.lowercase(Locale.getDefault()).contains(searchQuery)) {
                    filteredArtistSongList.add(artistSong)
                }
            }
        }
        _filteredListArtistSong.value = filteredArtistSongList
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

    internal fun filterFolderSongs(query: String) {
        val originalFolderSongList: MutableList<Song> =  (listFolderSong.value ?: listOf()).toMutableList()
        val filteredFolderSongList: MutableList<Song> = (filteredListFolderSong.value ?: listOf()).toMutableList()
        filteredFolderSongList.clear()
        if (query.isEmpty()) {
            filteredFolderSongList.addAll(originalFolderSongList)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault())
            for (folderSong in originalFolderSongList) {
                if (folderSong.title.lowercase(Locale.getDefault()).contains(searchQuery)||
                    folderSong.artist.lowercase(Locale.getDefault()).contains(searchQuery))  {
                    filteredFolderSongList.add(folderSong)
                }
            }
        }
        _filteredListFolderSong.value = filteredFolderSongList
    }

    internal fun filterFavoriteSongs(query: String) {
        val originalSongsList: MutableList<Song> =  (favoriteSongs.value ?: listOf()).toMutableList()
        val filteredSongsList: MutableList<Song> = (filteredFavoriteSongs.value ?: listOf()).toMutableList()
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
        _filteredFavoriteSongs.value = filteredSongsList
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

    fun getPositionPlaylist(): Int{  return helper.getPositionPlaylist() }
    fun savePositionPlaylist(position: Int){helper.savePositionPlaylist(position)}

    fun getPositionArtist(): Int{  return helper.getPositionArtist() }
    fun savePositionArtist(position: Int){helper.savePositionArtist(position)}

    fun getPositionFolder(): Int{  return helper.getPositionFolder() }
    fun savePositionFolder(position: Int){helper.savePositionFolder(position)}

    fun getTabsLocalPosition():Int = helper.getTabsLocalPosition()
    fun saveTabsLocalPosition(currentItem: Int) = helper.saveTabsLocalPosition(currentItem)

    fun setSelectedPosition(position: Int) { _selectedSongPosition.value = position }

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
        }
    }
    fun getSongAndPlaylist(): SongAndPlaylist{
        return songAndPlaylist.value
    }

    fun getIndexOfCurrentSong():Int{
        return indexOfCurrentSong.value
    }

    fun getIndexOfSavedSong():Int{
        return helper.getIndexOfCurrentSong()
    }

    fun getPositionFavoriteSong(): Int{  return helper.getPositionFavoriteSong() }
    fun savePositionFavoriteSong(position: Int){helper.savePositionFavoriteSong(position)}

    fun loadFavoriteSongs() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val songs = favoriteRepository.getOrderedFavorites()
                //val songs = favoriteRepository.getAllFavorites()
                _favoriteSongs.value = songs
                _filteredFavoriteSongs.value = songs
            } catch (e: Exception) {
                Log.d(TAG, "***SharedViewModel loadFavoriteSongs error = ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }


    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            val isNowFavorite = favoriteRepository.toggleFavorite(song.id)
            // Обновляем список избранного
            val songs = favoriteRepository.getAllFavorites()
            _favoriteSongs.value = songs
            _filteredFavoriteSongs.value = songs
        }
    }

    fun checkIsFavoriteWithCallback (songId: Long, callback:(Boolean)->Unit ) {
        viewModelScope.launch {
            val isFavorite = favoriteRepository.isFavorite(songId)
            callback(isFavorite)
        }
    }

    fun clearFavorites() {
        viewModelScope.launch {
            favoriteRepository.clearAllFavorites()
            loadFavoriteSongs() // Обновляем список
        }
    }

    fun setSelectedSong(song: Song) {
        _selectedSong.value = song
        if(song.artUri == null){
           val uri = getDefaultCoverUri(song)
            updateCoverImage(uri)
            //DefaultCoverUri = content://media/external/audio/albumart/3
            Log.d(TAG, "***SharedViewModel setSelectedSong song.artUri == null DefaultCoverUri = $uri")
        }else{
            val uri = (song.artUri!!).toUri()
            //val uri = "content://com.android.providers.media.documents/document/image%3A135257".toUri()
            updateCoverImage(uri)
            Log.d(TAG, "***SharedViewModel setSelectedSong song.artUri = $uri song = ${song.title}")
        }
    }

    fun getSelectedSong():Song? {
        return selectedSong.value
    }

    fun updateCoverImage(uri: Uri) {
        Log.d(TAG, "***SharedViewModel updateCoverImage  uri= $uri ")
        _coverImageUri.value = uri
    }

    fun getCoverImageUri():Uri? {
        return coverImageUri.value
    }

    fun updateCoverImageAndSave(uri: Uri) {
        Log.d(TAG, "2*** SharedViewModel updateCoverImageAndSave uri = $uri")
        _coverImageUri.value = uri
        saveCoverToDatabase(uri)
    }

    fun getDefaultCoverUri(song: Song): Uri{
        return ContentUris.withAppendedId(
            "content://media/external/audio/albumart".toUri(),
            song.albumId)
    }

    fun restoreDefaultCover() {
        _selectedSong.value?.let { song ->
            // Восстанавливаем обложку по умолчанию
            val defaultUri = getDefaultCoverUri(song)
            _coverImageUri.value = defaultUri
            song.artUri = defaultUri.toString()
        }
    }

    fun saveCoverToDatabase(uri: Uri) {
        viewModelScope.launch {
            _selectedSong.value?.let { selectedSong ->
//                // Сохраняем в InternalStorage //todo пока не используется
//                val coverPath =saveCoverToInternalStorage(uri, song)
//                _coverPath.value = coverPath
//                  Log.d(TAG, "111*** SharedViewModel saveCoverToDatabase coverPath = $coverPath")
                // Обновляем песню в основном списке песен - нужны оба - _songs и _filteredSongs
                _songs.value = _songs.value?.map {s->
                    if (s.id == selectedSong.id) s.copy(artUri = uri.toString()) else s
                }
                _filteredSongs.value = _filteredSongs.value?. map{filteredSong->
                    if (filteredSong.id == selectedSong.id) filteredSong.copy(artUri = uri.toString()) else filteredSong
                }
                //записываем путь к файлу обложки в базу
                repository.updateCoverPath(selectedSong.id, uri.toString())

                // Обновляем выбранную песню
                _selectedSong.value = selectedSong.copy(artUri = uri.toString())

                // Обновляем текущую песню если нужно
                _currentSong.value?.let { current ->
                    if (current.id == selectedSong.id) {
                        _currentSong.value = current.copy(artUri = uri.toString())
                    }
                }
            }
        }
    }

//

//    fun updateCoverPath(id:Long, coverPath:String){
//        viewModelScope.launch {
//            repository.updateCoverPath(id, coverPath)
//        }
//    }

    // Загрузка обложки
    suspend fun loadCoverImage(coverPath: String?) {
        coverPath?.let { path ->
            val bitmap = withContext(Dispatchers.IO) {
                try {
                    BitmapFactory.decodeFile(path)
                } catch (e: Exception) {
                    null
                }
            }
            _coverImage.postValue(bitmap)
        } ?: run {
            _coverImage.postValue(null)
        }
    }

    private fun saveCoverToInternalStorage(uri: Uri, song: Song): String {
        val context=App.instance
        // Создаем директорию
        val coversDir=File(context.filesDir, "covers")
        if (!coversDir.exists() && !coversDir.mkdirs()) {
            throw IOException("Failed to create covers directory")
        }

        val file=File(coversDir, "cover_${song.id}.jpg")

        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                // Убеждаемся что файл создан
                if (!file.exists() && !file.createNewFile()) {
                    throw IOException("Failed to create cover file")
                }

                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                    outputStream.flush()
                }
            } ?: throw IOException("Failed to open input stream from URI")

            file.absolutePath
        } catch (e: Exception) {
            Log.d(TAG, "Error saving cover for song ${song.title} error = ${e.message}")
            // Возвращаем путь к дефолтной обложке
            getDefaultCoverUri(song)
        }.toString()
    }

    // Загрузка обложки для текущей песни
    fun loadCurrentSongCover() {
        viewModelScope.launch {
            _currentSong.value?.artUri?.let { coverPath ->
                loadCoverImage(coverPath)
            }
        }
    }

//    // Обновление песни (при смене обложки)
//    fun updateSongCover(songId: Long, newCoverPath: String?) {
//        viewModelScope.launch {
//            // Обновляем в базе
//            repository.updateCoverPath(songId, newCoverPath.toString())
//
//            // Если это текущая песня - обновляем LiveData
//            if (_currentSong.value?.id == songId) {
//                _currentSong.value = _currentSong.value?.copy(artUri = newCoverPath)
//                loadCoverImage(newCoverPath)
//            }
//
//            // Можно добавить broadcast для уведомления других частей приложения
//        }
//    }


    //загрузка альбомов
    fun loadAlbums() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val albumsWithSongs = albumRepository.getAllAlbumsWithSongs()
                Log.d(TAG, " * SharedViewModel loadAlbums albumsWithSongs size = ${albumsWithSongs.size}")
                _albums.value = albumsWithSongs
                _filteredAlbums.value = albumsWithSongs  //todo убрать?
            } catch (e: Exception) {
                Log.d(TAG, " SharedViewModel loadAlbums Error loading albums ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    //загрузка артистов
    fun loadArtists() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val artistsWithSongsAndAlbums = artistsRepository.getAllArtistsWithSongsAndAlbums()
                Log.d(TAG, " * SharedViewModel loadArtists artistsWithSongsAndAlbums size = ${artistsWithSongsAndAlbums.size}")
                _artists.value = artistsWithSongsAndAlbums
                _filteredArtists.value = artistsWithSongsAndAlbums  //todo убрать?
            } catch (e: Exception) {
                Log.d(TAG, " SharedViewModel loadArtists Error loading Artists ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun loadFolders() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val foldersWithSongs = folderRepository.getAllFoldersWithSongs()
                _folders.value = foldersWithSongs
                _filteredFolders.value = foldersWithSongs //todo убрать?
            } catch (e: Exception) {
                Log.d(TAG, " SharedViewModel loadFolders Error loading Folders ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    //получение альбомов из песен и загрузка альбомов
    fun syncAlbums() {
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d(TAG, " * SharedViewModel syncAlbums albumRepository.syncAlbumsFromMediaFiles()")
                albumRepository.syncAlbumsFromMediaFiles()
                loadAlbums() // Перезагружаем после синхронизации
            } catch (e: Exception) {
                Log.d(TAG, " SharedViewModel syncAlbums Error loading albums error = ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun syncArtist (){
        viewModelScope.launch {
            _loading.value = true
            try {
                Log.d(TAG, " * SharedViewModel syncArtist artistsRepository.syncArtistsFromMediaFiles()")
                artistsRepository.syncArtistsFromMediaFiles()
                loadArtists() // Перезагружаем после синхронизации
            } catch (e: Exception) {
                Log.d(TAG, " SharedViewModel syncArtist Error loading Artist error = ${e.message}")
            } finally {
                _loading.value = false
            }
        }
    }

    fun syncFolders() {
        viewModelScope.launch {
            _loading.value = true
            try {
                folderRepository.syncFoldersFromMediaFiles()
                loadFolders() // Перезагружаем после синхронизации
            } catch (e: Exception) {
                Log.e("FolderViewModel", "Error syncing folders", e)
            } finally {
                _loading.value = false
            }
        }
    }

    fun getSongsByAlbum(albumId: Long){
        viewModelScope.launch {
            var listAlbumSong:List<Song> = albumRepository.getAlbumSongList(albumId)
            Log.d(TAG, " * SharedViewModel getSongsByAlbum listAlbumSong size = ${listAlbumSong.size}")
            _listAlbumSong.value = listAlbumSong
            _filteredListAlbumSong.value = listAlbumSong
        }
    }

    fun getSongsByArtist(artistId: Long){
        viewModelScope.launch {
            var listArtistSong:List<Song> = artistsRepository.getArtistSongList(artistId)
            Log.d(TAG, " * SharedViewModel getSongsByArtist listArtistSong size = ${listArtistSong.size}")
            _listArtistSong.value = listArtistSong
            _filteredListArtistSong.value = listArtistSong
        }
    }

    fun getSongsByFolder(folderPath: String) {
        viewModelScope.launch {
            var listFolderSong: List<Song> = folderRepository.getFolderSongList(folderPath)
            Log.d(TAG, " * SharedViewModel getSongsByFolder listFolderSong size = ${listFolderSong.size}")
             _listFolderSong.value = listFolderSong
            _filteredListFolderSong.value = listFolderSong
        }
    }

//    fun saveCoverToDatabase(uri: Uri) {
//        viewModelScope.launch {
//            _selectedSong.value?.let { selectedSong ->
////                // Сохраняем в InternalStorage //todo пока не используется
////                val coverPath =saveCoverToInternalStorage(uri, song)
////                _coverPath.value = coverPath
////                  Log.d(TAG, "111*** SharedViewModel saveCoverToDatabase coverPath = $coverPath")
//                // Обновляем песню в основном списке песен - нужны оба - _songs и _filteredSongs
//                _songs.value = _songs.value?.map {s->
//                    if (s.id == selectedSong.id) s.copy(artUri = uri.toString()) else s
//                }
//                _filteredSongs.value = _filteredSongs.value?. map{filteredSong->
//                    if (filteredSong.id == selectedSong.id) filteredSong.copy(artUri = uri.toString()) else filteredSong
//                }
//                //записываем путь к файлу обложки в базу
//                repository.updateCoverPath(selectedSong.id, uri.toString())
//                // Обновляем текущую песню если нужно
//                _currentSong.value?.let { current ->
//                    if (current.id == selectedSong.id) {
//                        _currentSong.value = current.copy(artUri = uri.toString())
//                    }
//                }
//            }
//        }
//    }

    fun updateSongInfo(
        songId: Long,
        title: String,
        artist: String,
        album: String?,
        author: String?,
        genre: String?,
        year: Int?
    ) {
        viewModelScope.launch {
            try {
                // Обновляем в базе данных
                repository.updateSongInfo(songId, title, artist, album, author, genre, year)

                // Обновляем в текущих данных
                _songs.value = _songs.value?.map { song ->
                    if (song.id == songId) {
                        song.copy(
                            title = title,
                            artist = artist,
                            albumName = album,
                            author = author,
                            genre = genre,
                            year = year
                        )
                    } else {
                        song
                    }
                }

                _filteredSongs.value = _filteredSongs.value?. map{filteredSong->
                    if (filteredSong.id == songId) filteredSong.copy(
                        title = title,
                        artist = artist,
                        albumName = album,
                        author = author,
                        genre = genre,
                        year = year
                    ) else filteredSong
                }

                // Обновляем выбранную песню если нужно
                _selectedSong.value?.let { selected ->
                    if (selected.id == songId) {
                        _selectedSong.value = selected.copy(
                            title = title,
                            artist = artist,
                            albumName = album,
                            author = author,
                            genre = genre,
                            year = year
                        )
                    }
                }

                // Обновляем текущую песню если нужно
                _currentSong.value?.let { current ->
                    if (current.id == songId) {
                        _currentSong.value = current.copy(
                            title = title,
                            artist = artist,
                            albumName = album,
                            author = author,
                            genre = genre,
                            year = year
                        )
                    }
                }
                // Уведомляем об успешном обновлении
                //_message.value = "Информация обновлена"
                Toast.makeText(App.instance, "Информация обновлена", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                //_message.value = "Ошибка обновления: ${e.message}"
                Log.d(TAG, "SharedViewModel updateSongInfo: Ошибка обновления: ${e.message} ")
                //Toast.makeText(App.instance, "Ошибка обновления: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun getSongDetails(songId: Long, callback: (SongFile?) -> Unit) {
        viewModelScope.launch {
            val songFile = repository.getSongFileById(songId)
            withContext(Dispatchers.Main) {
                callback(songFile)
            }
        }
    }

    fun updateFavoritesOrder(orderedSongs: List<Song>) {
        viewModelScope.launch {
            val favorites:List<FavoriteSong> = favoriteRepository.getAllFavoriteSongs()
            val updatedFavorites: MutableList<FavoriteSong> = mutableListOf<FavoriteSong>()

            orderedSongs.forEachIndexed { index, song ->
                val favorite = favorites.find { it.songId == song.id }
                favorite?.let {
                    updatedFavorites.add(it.copy(sortOrder = index))
                }
            }
            favoriteRepository.updateFavoriteOrder(updatedFavorites)

            // Обновляем LiveData
            _favoriteSongs.value = orderedSongs
            _filteredFavoriteSongs.value = orderedSongs
        }
    }

    fun getOrderedFavoriteSong() {
        viewModelScope.launch {
            val songs= favoriteRepository.getOrderedFavorites()
            _favoriteSongs.value=songs
            _filteredFavoriteSongs.value=songs
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            try {
                val playlists = playlistRepository.getAllPlaylists()
                _playlists.value = playlists
                _filteredPlaylists.value = playlists
            } catch (e: Exception) {
                Log.d(TAG,"SharedViewModel loadPlaylists Error loading playlists error = ${e.message}")
            }
        }
    }

    fun createPlaylist(name: String, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val playlistId = playlistRepository.createPlaylist(name)
                loadPlaylists() // Перезагружаем список
                onSuccess(playlistId)
            } catch (e: Exception) {
                Log.d(TAG,"SharedViewModel createPlaylist Error creating playlists error = ${e.message}")
            }
        }
    }

    fun addToPlaylist(playlistId: Long, songIds: List<Long>) {
        viewModelScope.launch {
            try {
                playlistRepository.addSongsToPlaylist(playlistId, songIds)
                loadPlaylists() // Перезагружаем список
            } catch (e: Exception) {
                Log.d(TAG,"SharedViewModel addToPlaylist Error adding songs to playlist error = ${e.message}")
            }
        }
    }

    fun loadPlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                val playlist = playlistRepository.getPlaylistWithSongs(playlistId)
                _currentPlaylist.value = playlist
            } catch (e: Exception) {
                Log.d(TAG,"SharedViewModel loadPlaylist Error loading playlist error = ${e.message}")
            }
        }
    }

    fun updatePlaylistOrder(playlists: List<Playlist>) {
        viewModelScope.launch {
            try {
                playlistRepository.updatePlaylistOrder(playlists)
                loadPlaylists()
            } catch (e: Exception) {
                Log.d(TAG,"SharedViewModel updatePlaylistOrder Error updating playlist order error = ${e.message}")
            }
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            try {
                playlistRepository.deletePlaylist(playlistId)
                loadPlaylists()
            } catch (e: Exception) {
                Log.d(TAG,"SharedViewModel deletePlaylist Error deleting playlist error = ${e.message}")
            }
        }
    }
}