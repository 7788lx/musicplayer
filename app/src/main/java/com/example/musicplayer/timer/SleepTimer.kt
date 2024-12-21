package com.example.musicplayer.timer

import android.os.CountDownTimer

class SleepTimer(
    private val durationMinutes: Int,
    private val onFinish: () -> Unit
) {
    private var timer: CountDownTimer? = null
    
    fun start() {
        timer?.cancel()
        timer = object : CountDownTimer(durationMinutes * 60 * 1000L, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 可以通过回调更新UI显示剩余时间
            }
            
            override fun onFinish() {
                this@SleepTimer.onFinish()
            }
        }.start()
    }
    
    fun cancel() {
        timer?.cancel()
        timer = null
    }
} 