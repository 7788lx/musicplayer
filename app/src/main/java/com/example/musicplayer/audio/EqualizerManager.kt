package com.example.musicplayer.audio

import android.content.Context
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.media.audiofx.PresetReverb

class EqualizerManager(context: Context, mediaPlayer: MediaPlayer) {
    private var equalizer: Equalizer? = null
    private var presetReverb: PresetReverb? = null
    private var isEnabled = false
    
    init {
        try {
            equalizer = Equalizer(0, mediaPlayer.audioSessionId).apply {
                enabled = false
            }
            presetReverb = PresetReverb(0, mediaPlayer.audioSessionId).apply {
                enabled = false
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun enable(enabled: Boolean) {
        isEnabled = enabled
        equalizer?.enabled = enabled
        presetReverb?.enabled = enabled
    }
    
    fun setPreset(preset: Short) {
        equalizer?.usePreset(preset)
    }
    
    fun setBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }
    
    fun getPresets(): List<String> {
        val presets = mutableListOf<String>()
        for (i in 0 until (equalizer?.numberOfPresets ?: 0)) {
            presets.add(equalizer?.getPresetName(i.toShort()) ?: "")
        }
        return presets
    }
    
    fun getBandLevels(): List<Short> {
        val levels = mutableListOf<Short>()
        for (i in 0 until (equalizer?.numberOfBands ?: 0)) {
            levels.add(equalizer?.getBandLevel(i.toShort()) ?: 0)
        }
        return levels
    }
    
    fun getBandFrequencies(): List<Int> {
        val frequencies = mutableListOf<Int>()
        for (i in 0 until (equalizer?.numberOfBands ?: 0)) {
            frequencies.add(equalizer?.getCenterFreq(i.toShort())?.div(1000) ?: 0)
        }
        return frequencies
    }
    
    fun release() {
        equalizer?.release()
        presetReverb?.release()
    }
} 