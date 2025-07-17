package com.example.muzpleer.scaner

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.example.muzpleer.model.MusicTrack


class MediaScanner(private val context: Context) {
    companion object{
        const val TAG = "33333"
    }

    fun scanDeviceForMusic(): List<MusicTrack> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            scanMusicApi29Plus(context)
        } else {
            scanMusicLegacy(context)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun scanMusicApi29Plus(context: Context): List<MusicTrack> {
        val musicList = mutableListOf<MusicTrack>()
        val collection = MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        )

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            collection,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Неизвестно"
                val artist = cursor.getString(artistColumn) ?: "Неизвестно"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(pathColumn)
                val album = cursor.getString(albumColumn) ?: "Неизвестно"
                val albumId = cursor.getLong(albumIdColumn)

                musicList.add(
                    MusicTrack(
                        id = id,
                        title = title,
                        artist = artist,
                        duration = duration,
                        mediaUri = path,
                        isLocal = true,
                        artworkUri = getAlbumArtUri(albumId),
                        album = album
                    )
                )
            }
        }

        return musicList
    }

    private fun scanMusicLegacy(context: Context): List<MusicTrack> {
        val musicList = mutableListOf<MusicTrack>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        context.contentResolver.query(
            uri,
            projection,
            selection,
            null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val title = cursor.getString(titleColumn) ?: "Неизвестно"
                val artist = cursor.getString(artistColumn) ?: "Неизвестно"
                val duration = cursor.getLong(durationColumn)
                val path = cursor.getString(pathColumn)
                val album = cursor.getString(albumColumn) ?: "Неизвестно"
                val albumId = cursor.getLong(albumIdColumn)

                musicList.add(
                    MusicTrack(
                        id = id,
                        title = title,
                        artist = artist,
                        duration = duration,
                        mediaUri = path,
                        isLocal = true,
                        artworkUri = getAlbumArtUri(albumId),
                        album = album
                    )
                )
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