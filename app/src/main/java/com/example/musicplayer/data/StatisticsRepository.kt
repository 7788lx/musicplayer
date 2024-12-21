package com.example.musicplayer.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class StatisticsRepository(context: Context) {
    private val prefs = context.getSharedPreferences(
        "music_statistics",
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    
    fun updatePlayCount(musicPath: String) {
        val stats = getStatistics(musicPath)
        stats.playCount++
        stats.lastPlayTime = System.currentTimeMillis()
        saveStatistics(stats)
    }
    
    fun updatePlayTime(musicPath: String, playTime: Long) {
        val stats = getStatistics(musicPath)
        stats.totalPlayTime += playTime
        saveStatistics(stats)
    }
    
    private fun getStatistics(musicPath: String): PlayStatistics {
        val json = prefs.getString(musicPath, null)
        return if (json != null) {
            gson.fromJson(json, PlayStatistics::class.java)
        } else {
            PlayStatistics(musicPath)
        }
    }
    
    private fun saveStatistics(stats: PlayStatistics) {
        val json = gson.toJson(stats)
        prefs.edit().putString(stats.musicPath, json).apply()
    }
    
    fun getMostPlayed(limit: Int = 10): List<PlayStatistics> {
        return prefs.all.mapNotNull { (path, json) ->
            gson.fromJson(json as String, PlayStatistics::class.java)
        }.sortedByDescending { it.playCount }
        .take(limit)
    }
    
    fun getRecentPlayed(limit: Int = 10): List<PlayStatistics> {
        return prefs.all.mapNotNull { (path, json) ->
            gson.fromJson(json as String, PlayStatistics::class.java)
        }.sortedByDescending { it.lastPlayTime }
        .take(limit)
    }
} 