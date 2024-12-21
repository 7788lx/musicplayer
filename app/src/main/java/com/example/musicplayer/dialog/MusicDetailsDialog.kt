package com.example.musicplayer.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import com.example.musicplayer.databinding.DialogMusicDetailsBinding
import com.example.musicplayer.model.MusicDetails
import java.text.DecimalFormat

class MusicDetailsDialog(
    context: Context,
    private val details: MusicDetails
) : Dialog(context) {

    private lateinit var binding: DialogMusicDetailsBinding
    
    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogMusicDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViews()
    }
    
    private fun setupViews() {
        binding.apply {
            titleText.text = details.title
            artistText.text = details.artist
            albumText.text = details.album
            durationText.text = formatDuration(details.duration)
            bitrateText.text = "${details.bitrate / 1000} kbps"
            sampleRateText.text = "${details.sampleRate / 1000} kHz"
            fileSizeText.text = formatFileSize(details.fileSize)
        }
    }
    
    private fun formatDuration(duration: Long): String {
        val minutes = duration / 60000
        val seconds = (duration % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
    
    private fun formatFileSize(size: Long): String {
        val df = DecimalFormat("#.##")
        return when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${df.format(size / 1024.0)} KB"
            else -> "${df.format(size / (1024.0 * 1024.0))} MB"
        }
    }
} 