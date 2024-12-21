package com.example.musicplayer.utils

import com.example.musicplayer.model.LyricLine
import java.io.File

class LyricParser {
    fun parseLyric(lrcFile: File): List<LyricLine> {
        if (!lrcFile.exists()) return emptyList()
        
        val lyrics = mutableListOf<LyricLine>()
        val timeRegex = "\\[(\\d{2}):(\\d{2})\\.(\\d{2})](.*)".toRegex()
        
        lrcFile.readLines().forEach { line ->
            timeRegex.matchEntire(line)?.let { matchResult ->
                val (minutes, seconds, hundredths, text) = matchResult.destructured
                val time = (minutes.toLong() * 60 * 1000) +
                          (seconds.toLong() * 1000) +
                          (hundredths.toLong() * 10)
                lyrics.add(LyricLine(time, text.trim()))
            }
        }
        
        return lyrics.sortedBy { it.time }
    }
} 