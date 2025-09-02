package com.example.muzpleer.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_files")
data class SongFile(
    @PrimaryKey
    val mediaStoreId: Long, // ID из MediaStore
    val title: String?,//название
    val artist: String?,//исполнитель
    val artistId: Long, //id исполнителя
    val duration: Long, //длительность трека
    val path: String,// мп3 для треков из local
    val artUri: String?,// путь к обложке для треков из local
    val isLocal: Boolean = true, //из ресурсов или local
    val album: String?, //название альбома из local
    val albumId: Long = -1,  //id альбома из local
    val folderPath: String = "", //путь к папке
    val lastModified: Long,
    val size: Long,
    val dateAdded: Long
)