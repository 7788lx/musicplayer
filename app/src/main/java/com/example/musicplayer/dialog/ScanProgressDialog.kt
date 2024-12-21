package com.example.musicplayer.dialog

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.example.musicplayer.databinding.DialogScanProgressBinding

class ScanProgressDialog(context: Context) : AlertDialog(context) {
    private val binding = DialogScanProgressBinding.inflate(LayoutInflater.from(context))

    init {
        setView(binding.root)
        setCancelable(false)
        binding.progressBar.isIndeterminate = true
    }

    fun updateProgress(current: Int, total: Int) {
        binding.apply {
            if (progressBar.isIndeterminate) {
                progressBar.isIndeterminate = false
            }
            progressBar.max = total
            progressBar.progress = current
            progressText.text = "$current / $total"
        }
    }
} 