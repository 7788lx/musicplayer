package com.example.musicplayer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import com.example.musicplayer.model.MusicFile
import android.util.Log
import android.widget.Toast

class MusicService : Service() {
    private var mediaPlayer: MediaPlayer? = null
    private var currentMusic: MusicFile? = null
    private var onCompletionListener: (() -> Unit)? = null
    private lateinit var mediaSession: MediaSessionCompat
    
    // 添加互斥锁
    private val mutex = Object()
    
    companion object {
        private const val CHANNEL_ID = "MusicPlayerChannel"
        private const val NOTIFICATION_ID = 1
        private const val ACTION_PLAY = "com.example.musicplayer.PLAY"
        private const val ACTION_PAUSE = "com.example.musicplayer.PAUSE"
        private const val ACTION_NEXT = "com.example.musicplayer.NEXT"
        private const val ACTION_PREVIOUS = "com.example.musicplayer.PREVIOUS"
    }
    
    private var onNextClickListener: (() -> Unit)? = null
    private var onPrevClickListener: (() -> Unit)? = null
    
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Music Player",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            setSound(null, null)
            enableLights(false)
            enableVibration(false)
        }
        
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
    
    private fun startForegroundService() {
        // 创建播放/暂停的 PendingIntent
        val playPauseIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(if (mediaPlayer?.isPlaying == true) ACTION_PAUSE else ACTION_PLAY).apply {
                `package` = packageName  // 添加包名，使广播只能被本应用接收
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 创建下一首的 PendingIntent
        val nextIntent = PendingIntent.getBroadcast(
            this,
            1,
            Intent(ACTION_NEXT).apply {
                `package` = packageName
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 创建上一首的 PendingIntent
        val prevIntent = PendingIntent.getBroadcast(
            this,
            2,
            Intent(ACTION_PREVIOUS).apply {
                `package` = packageName
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 创建打开应用的 PendingIntent
        val contentIntent = PendingIntent.getActivity(
            this,
            3,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(currentMusic?.name ?: "未知歌曲")
            .setContentText(currentMusic?.artist ?: "未知歌手")
            .setSmallIcon(R.drawable.ic_play)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            // 添加媒体控制按钮
            .addAction(
                R.drawable.ic_skip_previous,
                "上一首",
                prevIntent
            )
            .addAction(
                if (mediaPlayer?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play,
                if (mediaPlayer?.isPlaying == true) "暂停" else "播放",
                playPauseIntent
            )
            .addAction(
                R.drawable.ic_skip_next,
                "下一首",
                nextIntent
            )
            // 设置为媒体样式并关联 MediaSession
            .setStyle(MediaStyle()
                .setShowActionsInCompactView(0, 1, 2)
                .setMediaSession(mediaSession.sessionToken))
            .build()

        startForeground(NOTIFICATION_ID, notification)
        
        // 更新 MediaSession 状态
        mediaSession.setPlaybackState(PlaybackStateCompat.Builder()
            .setState(
                if (mediaPlayer?.isPlaying == true) 
                    PlaybackStateCompat.STATE_PLAYING 
                else 
                    PlaybackStateCompat.STATE_PAUSED,
                mediaPlayer?.currentPosition?.toLong() ?: 0,
                1f
            )
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            )
            .build()
        )
        
        // 更新媒体信息
        mediaSession.setMetadata(MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, currentMusic?.name)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, currentMusic?.artist)
            .build()
        )
        
        // 激活 MediaSession
        mediaSession.isActive = true
    }
    
    // 添加广播接收器
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> resumeMusic()
                ACTION_PAUSE -> pauseMusic()
                ACTION_NEXT -> onNextClickListener?.invoke()
                ACTION_PREVIOUS -> onPrevClickListener?.invoke()
            }
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // 初始化 MediaSession
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            // 使用新的方式设置播放控制
            setPlaybackState(PlaybackStateCompat.Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_PAUSE or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_SEEK_TO or
                    PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
                .setState(PlaybackStateCompat.STATE_STOPPED, 0, 1.0f)
                .build()
            )
            
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    resumeMusic()
                }

                override fun onPause() {
                    pauseMusic()
                }

                override fun onSkipToNext() {
                    onNextClickListener?.invoke()
                }

                override fun onSkipToPrevious() {
                    onPrevClickListener?.invoke()
                }

                override fun onSeekTo(pos: Long) {
                    mediaPlayer?.seekTo(pos.toInt())
                }
            })
        }
        
        // 注册广播接收器
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREVIOUS)
        }
        
        // 使用 registerReceiver 的新版本，添加 flags 参数
        registerReceiver(
            broadcastReceiver,
            filter,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Context.RECEIVER_NOT_EXPORTED
            } else {
                0
            }
        )
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    fun playMusic(music: MusicFile) {
        synchronized(mutex) {
            try {
                currentMusic = music
                
                // 释放旧的 MediaPlayer
                mediaPlayer?.release()
                
                // 创建新的 MediaPlayer
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(music.path)
                    setOnPreparedListener { 
                        start()
                        startForegroundService()
                    }
                    setOnCompletionListener { 
                        synchronized(mutex) {
                            onCompletionListener?.invoke()
                        }
                    }
                    setOnErrorListener { _, what, extra ->
                        synchronized(mutex) {
                            Log.e("MusicService", "MediaPlayer error: what=$what, extra=$extra")
                            // 尝试重新创建 MediaPlayer
                            try {
                                release()
                                mediaPlayer = MediaPlayer().apply {
                                    setDataSource(music.path)
                                    prepare()
                                    start()
                                    startForegroundService()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        true
                    }
                    prepareAsync()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun pauseMusic() {
        synchronized(mutex) {
            try {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun resumeMusic() {
        synchronized(mutex) {
            try {
                if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                    startForegroundService()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    fun isPlaying(): Boolean {
        return try {
            mediaPlayer?.isPlaying ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun getCurrentPosition(): Int {
        return try {
            mediaPlayer?.currentPosition ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    fun getDuration(): Int {
        return try {
            mediaPlayer?.duration ?: 0
        } catch (e: Exception) {
            e.printStackTrace()
            0
        }
    }
    
    fun seekTo(position: Int) {
        try {
            mediaPlayer?.seekTo(position)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun setOnCompletionListener(listener: () -> Unit) {
        onCompletionListener = listener
    }
    
    override fun onDestroy() {
        synchronized(mutex) {
            try {
                unregisterReceiver(broadcastReceiver)
                mediaPlayer?.release()
                mediaPlayer = null
                mediaSession.release()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                } else {
                    @Suppress("DEPRECATION")
                    stopForeground(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent): IBinder {
        return MusicBinder()
    }
    
    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
    
    // 添加设置监听器的方法
    fun setOnNextClickListener(listener: () -> Unit) {
        onNextClickListener = listener
    }
    
    fun setOnPrevClickListener(listener: () -> Unit) {
        onPrevClickListener = listener
    }
} 