package com.example.musicplayer.data

import android.content.Context
import com.example.musicplayer.model.Playlist
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.UUID

class PlaylistManager(private val context: Context) {
    private val gson = Gson()
    private val sharedPreferences = context.getSharedPreferences("playlists", Context.MODE_PRIVATE)
    
    fun getPlaylists(): List<Playlist> {
        val json = sharedPreferences.getString("playlists", "[]")
        val type = object : TypeToken<List<Playlist>>() {}.type
        return gson.fromJson(json, type)
    }
    
    private fun savePlaylists(playlists: List<Playlist>) {
        val json = gson.toJson(playlists)
        sharedPreferences.edit().putString("playlists", json).apply()
    }
    
    fun createPlaylist(name: String): Playlist {
        val playlists = getPlaylists().toMutableList()
        val newPlaylist = Playlist(
            id = UUID.randomUUID().toString(),
            name = name,
            musicPaths = emptyList()
        )
        playlists.add(newPlaylist)
        savePlaylists(playlists)
        return newPlaylist
    }
    
    fun deletePlaylist(playlistId: String) {
        val playlists = getPlaylists().filter { it.id != playlistId }
        savePlaylists(playlists)
    }
    
    fun renamePlaylist(playlistId: String, newName: String) {
        val playlists = getPlaylists().map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(name = newName)
            } else {
                playlist
            }
        }
        savePlaylists(playlists)
    }
    
    fun addToPlaylist(playlistId: String, musicPath: String) {
        val playlists = getPlaylists().map { playlist ->
            if (playlist.id == playlistId && !playlist.musicPaths.contains(musicPath)) {
                playlist.copy(musicPaths = playlist.musicPaths + musicPath)
            } else {
                playlist
            }
        }
        savePlaylists(playlists)
    }
    
    fun removeFromPlaylist(playlistId: String, musicPath: String) {
        val playlists = getPlaylists().map { playlist ->
            if (playlist.id == playlistId) {
                playlist.copy(musicPaths = playlist.musicPaths - musicPath)
            } else {
                playlist
            }
        }
        savePlaylists(playlists)
    }
} 