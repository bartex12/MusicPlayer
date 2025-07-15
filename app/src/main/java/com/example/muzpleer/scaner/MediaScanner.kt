package com.example.muzpleer.scaner

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.muzpleer.model.MusicTrack


class MediaScanner(private val context: Context) {
    companion object{
        const val TAG = "33333"
    }

    fun scanDeviceForMusic(): List<MusicTrack> {
        val musicList = mutableListOf<MusicTrack>()
        val resolver: ContentResolver = context.contentResolver
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        Log.d(TAG, "MediaScanner scanDeviceForMusic uri = ${uri.toString()}:  ")

        val cursor: Cursor? = resolver.query(
            uri,
            null,
            selection,
            null,
            sortOrder
        )

        Log.d(TAG, "MediaScanner scanDeviceForMusic cursor size = ${cursor?.count}:  ")

        cursor?.use {
            while (it.moveToNext()) {
                val title = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val duration = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val data = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val albumId = it.getLong(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                val album = it.getString(it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))

                val track =  MusicTrack(
                    id = data.hashCode().toString(),
                    title = title ?: "Unknown",
                    artist = artist ?: "Unknown",
                    duration = duration,
                    mediaUri = data,
                    isLocal = true,
                    artworkUri = getAlbumArtUri(albumId),
                    album = album
                )
                musicList.add(track)
                Log.d(TAG, "***MediaScanner scanDeviceForMusic : " +
                        "${track.title} //${track.artist} /*/ size = ${musicList.size} ")
            }
        }
        return musicList
    }

    private fun getAlbumArtUri(albumId: Long): String? {
        val contentUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Audio.Albums.ALBUM_ART)
        val selection = "${MediaStore.Audio.Albums._ID} = ?"
        val selectionArgs = arrayOf(albumId.toString())

        context.contentResolver.query(
            contentUri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getString(0)
            }
        }
        return null
    }
}