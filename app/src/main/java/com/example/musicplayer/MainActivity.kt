package com.example.musicplayer

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.app.ActivityCompat

import com.example.musicplayer.adapter.MusicAdapter
import com.example.musicplayer.adapter.MusicListAdapter
import com.example.musicplayer.databinding.ActivityMainBinding
import com.example.musicplayer.model.MusicFile
import com.example.musicplayer.dialog.ScanProgressDialog

import kotlinx.coroutines.*
import java.io.File
import kotlin.random.Random
import java.util.Locale
import kotlinx.coroutines.delay
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.EditorInfo
import android.app.AlertDialog
import android.widget.EditText
import java.util.concurrent.atomic.AtomicInteger
import java.util.ArrayDeque
import android.widget.ArrayAdapter

class MainActivity : ComponentActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private var musicAdapter: MusicAdapter? = null
    private lateinit var musicListAdapter: MusicListAdapter
    private var musicService: MusicService? = null
    private var currentPlayMode = PlayMode.SEQUENCE
    private var currentMusicIndex = 0
    private var musicList = mutableListOf<MusicFile>()
    private var isServiceBound = false
    
    // 使用 volatile 确保线程间的可见性
    @Volatile
    private var isActivityDestroyed = false
    
    // 进度更新Handler
    private val handler = Handler(Looper.getMainLooper())
    private val updateProgress = object : Runnable {
        override fun run() {
            if (!isActivityDestroyed) {
                musicService?.let { service ->
                    if (service.isPlaying()) {
                        updateProgressUI(
                            service.getCurrentPosition(),
                            service.getDuration()
                        )
                    }
                }
                handler.postDelayed(this, 1000)
            }
        }
    }
    
    // Service接
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isServiceBound = true
            
            // 设置各种监听器
            musicService?.setOnCompletionListener {
                playNext()
            }
            musicService?.setOnNextClickListener {
                playNext()
            }
            musicService?.setOnPrevClickListener {
                playPrevious()
            }
            
            // 服务连接后，如果有保存的音乐列表，恢复播放状态
            if (musicList.isNotEmpty()) {
                restorePlayerState()
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isServiceBound = false
        }
    }
    
    // 添加权限请求启动器
    private val storagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            lifecycleScope.launch {
                scanMusic()
            }
        } else {
            Toast.makeText(this, "没有存储权限，无法播放音乐", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private var originalMusicList = mutableListOf<MusicFile>()  // 保存完整的音乐列表
    
    // 添加排序相关的枚举
    enum class SortField {
        NAME, ARTIST, MODIFIED_TIME
    }

    enum class SortOrder {
        ASCENDING, DESCENDING
    }

    private var currentSortField = SortField.NAME
    private var currentSortOrder = SortOrder.ASCENDING
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // 1. 设置窗口标志
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            
            // 2. 初始化视图绑定
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)
            
            // 3. 初始化基本视图
            initViews()
            
            // 4. 启动并绑定服务
            bindMusicService()
            
            // 5. 加载已保存的音乐列表
            loadSavedMusicList()
            
            // 6. 如果没有音乐，请求权限并扫描
            if (musicList.isEmpty()) {
                requestPermissions()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MainActivity", "onCreate failed", e)
            Toast.makeText(this, "应用启动失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    private fun initViews() {
        try {
            // 创建并设置适配器
            musicListAdapter = MusicListAdapter(this, musicList)
            binding.musicList.adapter = musicListAdapter
            
            // 设置点击事件
            binding.musicList.setOnItemClickListener { _, _, position, _ ->
                playMusic(position)
            }
            
            // 3. 设置基本的点击事件
            binding.playPauseButton.setOnClickListener { togglePlayPause() }
            binding.prevButton.setOnClickListener { playPrevious() }
            binding.nextButton.setOnClickListener { playNext() }
            binding.playModeButton.setOnClickListener { togglePlayMode() }
            binding.sortButton.setOnClickListener { showSortDialog() }
            
            // 4. 设置搜索按钮点击事件
            binding.searchButton.setOnClickListener {
                showSearchDialog()
            }
            
            // 5. 设置进度条
            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) musicService?.seekTo(progress)
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            
            // 设置返回按钮击事
            binding.backButton.setOnClickListener {
                musicList = originalMusicList.toMutableList()
                updateListView()
                binding.musicList.setSelection(0)
                binding.backButton.visibility = View.GONE
            }
            
            // 设置返回顶部按钮点击事件
            binding.topButton.setOnClickListener {
                binding.musicList.setSelection(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateProgressUI(position: Int, duration: Int) {
        lifecycleScope.launch(Dispatchers.Main.immediate) {  // 使用 immediate 调度器
            try {
                if (!isActivityDestroyed) {
                    binding.apply {
                        seekBar.max = duration
                        seekBar.progress = position
                        currentTime.text = formatTime(position)
                        totalTime.text = formatTime(duration)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun formatTime(ms: Int): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
    
    private fun bindMusicService() {
        try {
            val intent = Intent(this, MusicService::class.java)
            // 启动服务
            startService(intent)
            // 再绑定服务
            bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "音乐服务启动失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    try {
                        val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "打开设置页面失败，请手动前往设置授权", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                lifecycleScope.launch {
                    scanMusic()
                }
            }
        } else {
            // Android 10 及以下版本使用系统权限请求对话框
            storagePermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
    
    private suspend fun scanMusic() {
        var dialog: ScanProgressDialog? = null
        try {
            withContext(Dispatchers.Main) {
                dialog = ScanProgressDialog(this@MainActivity)
                dialog?.show()
            }

            val musicFiles = withContext(Dispatchers.IO) {
                val rootDir = Environment.getExternalStorageDirectory()
                val mp3Dir = File(rootDir, "mp3")
                
                if (!mp3Dir.exists()) {
                    mp3Dir.mkdirs()
                }

                // 使用 sequence 惰性收集所有 MP3 文件
                val mp3Files = sequence {
                    val stack = ArrayDeque<File>()
                    stack.add(mp3Dir)

                    while (stack.isNotEmpty()) {
                        val dir = stack.removeFirst()
                        dir.listFiles()?.forEach { file ->
                            if (file.isDirectory) {
                                stack.add(file)
                            } else if (file.extension.lowercase() == "mp3") {
                                yield(file)
                            }
                        }
                    }
                }.toList()

                // 使用协程并发处理文件
                val tempList = mutableListOf<MusicFile>()
                val scannedCount = AtomicInteger(0)
                
                withContext(Dispatchers.IO) {
                    mp3Files.chunked(100).map { chunk ->
                        async {
                            chunk.mapNotNull { file ->
                                MusicFile.fromPath(file.absolutePath)?.also {
                                    val count = scannedCount.incrementAndGet()
                                    withContext(Dispatchers.Main) {
                                        dialog?.updateProgress(count, mp3Files.size)
                                    }
                                }
                            }
                        }
                    }.awaitAll().flatten().let {
                        tempList.addAll(it)
                    }
                }

                tempList
            }

            withContext(Dispatchers.Main) {
                dialog?.dismiss()
                dialog = null

                if (musicFiles.isNotEmpty()) {
                    originalMusicList = musicFiles.toMutableList()
                    musicList = musicFiles.toMutableList()
                    updateListView()
                    saveMusicList()
                    
                    if (musicList.size == musicFiles.size) {
                        restorePlayerState()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                dialog?.dismiss()
                dialog = null
                Toast.makeText(
                    this@MainActivity,
                    "扫描音乐文件失败: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } finally {
            withContext(Dispatchers.Main) {
                dialog?.dismiss()
                dialog = null
            }
        }
    }
    
    private fun playMusic(index: Int) {
        if (!isActivityDestroyed && index in musicList.indices) {
            currentMusicIndex = index
            lifecycleScope.launch(Dispatchers.Main.immediate) {
                try {
                    // 只更新播放状态
                    musicAdapter?.currentPlayingIndex = index
                    binding.musicList.post {
                        binding.musicList.setSelection(index)
                    }
                    
                    // 然后播放音乐
                    musicService?.playMusic(musicList[index])
                    binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                    
                    // 重新启动进度更新
                    handler.removeCallbacks(updateProgress)
                    handler.post(updateProgress)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    private fun togglePlayPause() {
        if (!isActivityDestroyed) {
            musicService?.let { service ->
                lifecycleScope.launch(Dispatchers.Main) {
                    try {
                        if (service.isPlaying()) {
                            service.pauseMusic()
                            binding.playPauseButton.setImageResource(R.drawable.ic_play)
                            // 暂停时停止度条更新
                            handler.removeCallbacks(updateProgress)
                        } else {
                            service.resumeMusic()
                            binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                            // 恢复播放时重新启动进度条更新
                            handler.post(updateProgress)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
    
    private fun playNext() {
        if (musicList.isEmpty()) return
        
        val nextIndex = when (currentPlayMode) {
            PlayMode.SEQUENCE -> {
                if (currentMusicIndex + 1 >= musicList.size) 0 else currentMusicIndex + 1
            }
            PlayMode.RANDOM -> Random.nextInt(musicList.size)
        }
        playMusic(nextIndex)
    }
    
    private fun playPrevious() {
        if (musicList.isEmpty()) return
        
        val prevIndex = when (currentPlayMode) {
            PlayMode.SEQUENCE -> {
                if (currentMusicIndex > 0) currentMusicIndex - 1 else musicList.size - 1
            }
            PlayMode.RANDOM -> Random.nextInt(musicList.size)
        }
        playMusic(prevIndex)
    }
    
    private fun togglePlayMode() {
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                currentPlayMode = if (currentPlayMode == PlayMode.SEQUENCE) {
                    binding.playModeButton.apply {
                        setImageResource(R.drawable.ic_shuffle)
                        setColorFilter(Color.BLACK)
                    }
                    PlayMode.RANDOM
                } else {
                    binding.playModeButton.apply {
                        setImageResource(R.drawable.ic_repeat)
                        setColorFilter(Color.GRAY)
                    }
                    PlayMode.SEQUENCE
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // 只在首次授权后扫描音乐
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager() && musicList.isEmpty()) {
                lifecycleScope.launch {
                    scanMusic()
                }
            }
        }
        
        // 恢复进度更新
        handler.post(updateProgress)
        
        // 更新界面状态
        musicService?.let { service ->
            if (service.isPlaying()) {
                binding.playPauseButton.setImageResource(R.drawable.ic_pause)
                // 只更新播放状态
                musicAdapter?.currentPlayingIndex = currentMusicIndex
            } else {
                binding.playPauseButton.setImageResource(R.drawable.ic_play)
            }
        }
    }
    
    override fun onPause() {
        handler.removeCallbacks(updateProgress)
        savePlayerState()
        saveMusicList()  // 保存音乐列表
        super.onPause()
    }
    
    override fun onDestroy() {
        isActivityDestroyed = true
        savePlayerState()
        
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                // 清理 RecyclerView
                binding.musicList.adapter = null
                
                // 移除所有回调
                handler.removeCallbacksAndMessages(null)
                
                // 解绑服务
                if (isServiceBound) {
                    unbindService(serviceConnection)
                    isServiceBound = false
                }
                
                // 清空其他资源
                musicService = null
                musicAdapter = null
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        
        super.onDestroy()
    }
    
    companion object {
        private const val PREF_NAME = "MusicPlayerPrefs"
        private const val KEY_LAST_PLAYED_INDEX = "lastPlayedIndex"
        private const val KEY_LAST_PLAYED_POSITION = "lastPlayedPosition"
        private const val KEY_PLAY_MODE = "playMode"
        private const val KEY_MUSIC_LIST = "musicList"  // 新增
    }
    
    enum class PlayMode {
        SEQUENCE,   // 顺序播放
        RANDOM      // 随机播放
    }

    // 在 onCreate 之后添加保存状态的方法
    private fun savePlayerState() {
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().apply {
            putInt(KEY_LAST_PLAYED_INDEX, currentMusicIndex)
            putInt(KEY_LAST_PLAYED_POSITION, musicService?.getCurrentPosition() ?: 0)
            putString(KEY_PLAY_MODE, currentPlayMode.name)
            apply()
        }
    }

    // 添加恢复状���的方
    private fun restorePlayerState() {
        try {
            val prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            val lastIndex = prefs.getInt(KEY_LAST_PLAYED_INDEX, -1)
            val lastPosition = prefs.getInt(KEY_LAST_PLAYED_POSITION, 0)
            val lastPlayMode = prefs.getString(KEY_PLAY_MODE, PlayMode.SEQUENCE.name)

            // 恢复播放模式
            currentPlayMode = PlayMode.valueOf(lastPlayMode ?: PlayMode.SEQUENCE.name)
            if (currentPlayMode == PlayMode.RANDOM) {
                binding.playModeButton.apply {
                    setImageResource(R.drawable.ic_shuffle)
                    setColorFilter(Color.BLACK)
                }
            }

            if (lastIndex in musicList.indices) {
                // 有上次播放记录，恢复播放
                currentMusicIndex = lastIndex
                playMusic(lastIndex)
                lifecycleScope.launch {
                    delay(500) // 等待播放器初始化
                    musicService?.seekTo(lastPosition)
                }
            } else if (musicList.isNotEmpty()) {
                // 没有上次播放记录，随机播放一首
                val randomIndex = Random.nextInt(musicList.size)
                currentMusicIndex = randomIndex
                playMusic(randomIndex)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadSavedMusicList() {
        val gson = Gson()
        val json = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MUSIC_LIST, null)
        
        if (!json.isNullOrEmpty()) {
            try {
                // 使用 TypeToken 获取正确类型，移除显式类参数
                val listType = object : TypeToken<ArrayList<MusicFile>>() {}.type
                val savedList = gson.fromJson<ArrayList<MusicFile>>(json, listType)
                
                // 验证文件是否还存在
                originalMusicList = savedList.filter { file ->
                    File(file.path).exists()
                }.toMutableList()
                
                musicList = originalMusicList.toMutableList()
                updateListView()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun saveMusicList() {
        try {
            val gson = Gson()
            val json = gson.toJson(musicList)
            getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_MUSIC_LIST, json)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun filterMusicList(query: String) {
        if (query.isEmpty()) {
            musicList = originalMusicList.toMutableList()
            updateListView()
            binding.musicList.setSelection(0)
            binding.backButton.visibility = View.GONE
        } else {
            val filteredList = originalMusicList.filter { music ->
                music.artist.contains(query) || music.name.contains(query)
            }
            
            if (filteredList.isEmpty()) {
                Toast.makeText(
                    this,
                    "没有找到包含\"$query\"的歌曲",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                musicList = filteredList.toMutableList()
                updateListView()
                binding.musicList.setSelection(0)
                binding.backButton.visibility = View.VISIBLE
            }
        }
        
        // 应用当前的排序，但不显示提示
        if (musicList.isNotEmpty()) {
            applySorting(currentSortField, currentSortOrder)
        }
    }

    // 添加扩展函数判断是否为汉字
    private fun Char.isChineseCharacter(): Boolean {
        return this.code in 0x4E00..0x9FFF
    }

    // 新增一个不显示提示的序方法
    private fun applySorting(field: SortField, order: SortOrder) {
        val comparator = when (field) {
            SortField.NAME -> compareBy<MusicFile> { it.name }
            SortField.ARTIST -> compareBy<MusicFile> { it.artist }
            SortField.MODIFIED_TIME -> compareBy<MusicFile> { File(it.path).lastModified() }
        }

        val currentMusic = if (currentMusicIndex >= 0 && currentMusicIndex < musicList.size) {
            musicList[currentMusicIndex]
        } else null

        val sortedList = if (order == SortOrder.ASCENDING) {
            musicList.sortedWith(comparator)
        } else {
            musicList.sortedWith(comparator.reversed())
        }

        musicList = sortedList.toMutableList()
        updateListView()
        
        currentMusic?.let { music ->
            val newIndex = musicList.indexOfFirst { it.path == music.path }
            if (newIndex != -1) {
                currentMusicIndex = newIndex
                binding.musicList.post {
                    binding.musicList.setSelection(newIndex)
                }
            }
        }
    }

    private fun sortMusicList(field: SortField, order: SortOrder) {
        currentSortField = field
        currentSortOrder = order

        val comparator = when (field) {
            SortField.NAME -> compareBy<MusicFile> { it.name }
            SortField.ARTIST -> compareBy<MusicFile> { it.artist }
            SortField.MODIFIED_TIME -> compareBy<MusicFile> { File(it.path).lastModified() }
        }

        val currentMusic = if (currentMusicIndex >= 0 && currentMusicIndex < musicList.size) {
            musicList[currentMusicIndex]
        } else null

        val sortedList = if (order == SortOrder.ASCENDING) {
            musicList.sortedWith(comparator)
        } else {
            musicList.sortedWith(comparator.reversed())
        }

        musicList = sortedList.toMutableList()
        updateListView()

        // 直接滚动到顶部
        binding.musicList.setSelection(0)
        
        // 如果有正播放的曲，延迟一会再滚动到播放位置
        currentMusic?.let { music ->
            val newIndex = musicList.indexOfFirst { it.path == music.path }
            if (newIndex != -1) {
                currentMusicIndex = newIndex
                binding.musicList.postDelayed({
                    binding.musicList.setSelection(newIndex)
                }, 500)  // 延迟500ms
            }
        }

        // 显示排序结果提示
        val fieldName = when (field) {
            SortField.NAME -> "歌名"
            SortField.ARTIST -> "歌手"
            SortField.MODIFIED_TIME -> "修改时间"
        }
        val orderName = if (order == SortOrder.ASCENDING) "升序" else "降序"
        
        Toast.makeText(
            this,
            "已按${fieldName}${orderName}排序",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showSortDialog() {
        val items = arrayOf(
            "按歌名排序",
            "按歌手排序",
            "按修改时间排序"
        )

        AlertDialog.Builder(this)
            .setTitle("选择排序方式")
            .setItems(items) { _, which ->
                when (which) {
                    0 -> showSortOrderDialog(SortField.NAME)
                    1 -> showSortOrderDialog(SortField.ARTIST)
                    2 -> showSortOrderDialog(SortField.MODIFIED_TIME)
                }
            }
            .show()
    }

    private fun showSortOrderDialog(field: SortField) {
        val items = arrayOf("升序", "降序")
        AlertDialog.Builder(this)
            .setTitle("选择排序顺")
            .setItems(items) { _, which ->
                val order = if (which == 0) SortOrder.ASCENDING else SortOrder.DESCENDING
                sortMusicList(field, order)
            }
            .show()
    }

    private fun showSearchDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_search, null)
        val searchEditText = dialogView.findViewById<EditText>(R.id.searchEditText)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("搜索")
            .setView(dialogView)
            .setPositiveButton("搜索") { _, _ ->
                val query = searchEditText.text.toString().trim()
                if (query.isNotEmpty()) {
                    filterMusicList(query)
                }
            }
            .setNegativeButton("取消", null)
            .create()

        // 设置软键盘搜索按钮点击事件
        searchEditText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val query = textView.text.toString().trim()
                if (query.isNotEmpty()) {
                    filterMusicList(query)
                    dialog.dismiss()
                }
                true
            } else {
                false
            }
        }

        dialog.show()

        // 自动显示软键盘
        searchEditText.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun updateListView() {
        musicListAdapter.updateData(musicList)
    }
}