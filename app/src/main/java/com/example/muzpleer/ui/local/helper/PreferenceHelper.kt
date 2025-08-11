package com.example.muzpleer.ui.local.helper

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.core.content.edit

class PreferenceHelperImpl(private  val app: Application): IPreferenceHelper {
    companion object{
        const val TAG = "33333"
        const val FIRST_POSITION_SONG = "FIRST_POSITION_SONG"
        const val FIRST_POSITION_ALBUM = "FIRST_POSITION_ALBUM"
        const val FIRST_POSITION_ARTIST ="FIRST_POSITION_ARTIST"
        const val FIRST_POSITION_FOLDER ="FIRST_POSITION_FOLDER"
        const val PAGER_LOCAL_POSITION ="PAGER_LOCAL_POSITION"
    }

    private val prefs: SharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(app)
    }

    private fun putValue(pair:Pair<String, Any>) =
        prefs.edit{

        val key=pair.first
        val value=pair.second

        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Boolean -> putBoolean(key, value)
            is Float -> putFloat(key, value)
            is Long -> putLong(key, value)
            else -> error("Only primitives!!!")
        }
    }

    override fun savePositionSong(position: Int) {
        putValue(FIRST_POSITION_SONG to position )
        Log.d(TAG,"PreferenceHelper savePositionSong position = $position" )
    }

    override fun getPositionSong(): Int {
        val position = prefs .getInt(FIRST_POSITION_SONG, 0)
        Log.d(TAG, "PreferenceHelper getPositionSong position = $position")
        return position
    }

    override fun savePositionAlbum(position: Int) {
        putValue(FIRST_POSITION_ALBUM to position )
        Log.d(TAG,"PreferenceHelper savePositionAlbum position = $position" )
    }

    override fun getPositionAlbum(): Int {
        val position = prefs .getInt(FIRST_POSITION_ALBUM, 0)
        Log.d(TAG, "PreferenceHelper getPositionAlbum position = $position")
        return position
    }

    override fun savePositionArtist(position: Int) {
        putValue(FIRST_POSITION_ARTIST to position )
        Log.d(TAG,"PreferenceHelper savePositionArtist position = $position" )
    }

    override fun getPositionArtist(): Int {
        val position = prefs .getInt(FIRST_POSITION_ARTIST, 0)
        Log.d(TAG, "PreferenceHelper getPositionArtist position = $position")
        return position
    }

    override fun savePositionFolder(position: Int) {
        putValue(FIRST_POSITION_FOLDER to position )
        Log.d(TAG,"PreferenceHelper savePositionFolder position = $position" )
    }

    override fun getPositionFolder(): Int {
        val position = prefs .getInt(FIRST_POSITION_FOLDER, 0)
        Log.d(TAG, "PreferenceHelper getPositionFolder position = $position")
        return position
    }

    override fun saveTabsLocalPosition(position:Int) {
        putValue(PAGER_LOCAL_POSITION to position)
    }

    override fun getTabsLocalPosition(): Int {
        return  prefs.getInt(PAGER_LOCAL_POSITION, 0)
    }

    fun getSoundLevel(): Int {
        return prefs.getString("sound_level", "80")?.toInt()?: 80
    }

}