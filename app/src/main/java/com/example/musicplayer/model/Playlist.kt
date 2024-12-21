package com.example.musicplayer.model

data class Playlist(
    val id: String,
    val name: String,
    val musicPaths: List<String>
) 