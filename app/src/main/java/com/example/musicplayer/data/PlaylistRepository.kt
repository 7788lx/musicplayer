package com.example.musicplayer.data

import android.content.Context
import com.example.musicplayer.model.MusicFile
import com.example.musicplayer.model.Playlist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class PlaylistRepository(context: Context) {
    private val playlistManager = PlaylistManager(context)
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()
    
    init {
        loadPlaylists()
    }
    
    private fun loadPlaylists() {
        _playlists.value = playlistManager.getPlaylists()
    }
    
    fun createPlaylist(name: String): Playlist {
        val playlist = playlistManager.createPlaylist(name)
        loadPlaylists()
        return playlist
    }
    
    fun deletePlaylist(playlistId: String) {
        playlistManager.deletePlaylist(playlistId)
        loadPlaylists()
    }
    
    fun renamePlaylist(playlistId: String, newName: String) {
        playlistManager.renamePlaylist(playlistId, newName)
        loadPlaylists()
    }
    
    fun addToPlaylist(playlistId: String, musicPath: String) {
        playlistManager.addToPlaylist(playlistId, musicPath)
        loadPlaylists()
    }
    
    fun removeFromPlaylist(playlistId: String, musicPath: String) {
        playlistManager.removeFromPlaylist(playlistId, musicPath)
        loadPlaylists()
    }
    
    fun getPlaylistMusic(playlistId: String): List<MusicFile> {
        val playlist = playlistManager.getPlaylists().find { it.id == playlistId }
        return playlist?.musicPaths?.mapNotNull { MusicFile.fromPath(it) } ?: emptyList()
    }
    
    fun getAllPlaylists(): List<Playlist> {
        return _playlists.value
    }
    
    fun isMusicInPlaylist(playlistId: String, musicPath: String): Boolean {
        return _playlists.value.find { it.id == playlistId }?.musicPaths?.contains(musicPath) ?: false
    }
} 