package com.example.muzpleer.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_files")
data class SongFile(
    @PrimaryKey
    val mediaStoreId: Long, // ID из MediaStore
    val title: String?,
    val artist: String?,
    val duration: Long,
    val path: String,
    val artUri: String?,
    val isLocal: Boolean = true,
    val album: String?,
    val albumId: Long = -1,
    val folderPath: String = "",
    val lastModified: Long,
    val size: Long,
    val dateAdded: Long
)