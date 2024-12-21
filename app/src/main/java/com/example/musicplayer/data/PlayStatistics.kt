package com.example.musicplayer.data

data class PlayStatistics(
    val musicPath: String,
    var playCount: Int = 0,
    var lastPlayTime: Long = 0,
    var totalPlayTime: Long = 0
) 