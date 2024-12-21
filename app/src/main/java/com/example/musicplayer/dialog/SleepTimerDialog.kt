package com.example.musicplayer.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.adapter.SleepTimerAdapter
import com.example.musicplayer.databinding.DialogSleepTimerBinding
import com.example.musicplayer.model.SleepTimer

class SleepTimerDialog(
    context: Context,
    private val onTimerSelected: (SleepTimer) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogSleepTimerBinding
    private lateinit var adapter: SleepTimerAdapter
    
    private val timerOptions = listOf(
        SleepTimer(15, "15分钟"),
        SleepTimer(30, "30分钟"),
        SleepTimer(45, "45分钟"),
        SleepTimer(60, "1小时"),
        SleepTimer(90, "1.5小时"),
        SleepTimer(120, "2小时")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogSleepTimerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        adapter = SleepTimerAdapter { timer ->
            onTimerSelected(timer)
            dismiss()
        }

        binding.timerList.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@SleepTimerDialog.adapter
        }

        adapter.submitList(timerOptions)
    }
} 