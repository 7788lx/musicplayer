package com.example.musicplayer.model

data class MusicDetails(
    val title: String,
    val artist: String,
    val album: String,
    val duration: Long,
    val bitrate: Int,
    val sampleRate: Int,
    val fileSize: Long
) 