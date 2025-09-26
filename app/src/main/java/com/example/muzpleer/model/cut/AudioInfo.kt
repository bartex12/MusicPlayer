package com.example.muzpleer.model.cut

data class AudioInfo(
    val filePath: String,
    val duration: Long, // в миллисекундах
    val sampleRate: Int,
    val channels: Int,
    val bitrate: Int
)
