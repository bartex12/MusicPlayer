package com.example.muzpleer.ui.local.helper

interface IPreferenceHelper {
    fun savePositionSong(position:Int)
    fun getPositionSong(): Int

    fun savePositionAlbum(position:Int)
    fun getPositionAlbum(): Int

    fun savePositionArtist(position:Int)
    fun getPositionArtist(): Int

    fun savePositionFolder(position:Int)
    fun getPositionFolder(): Int

    fun saveTabsLocalPosition(tabLocaPosition:Int)
    fun getTabsLocalPosition(): Int

    fun saveCurrentSongId(id:Long)
    fun getCurrentSongId(): Long
}