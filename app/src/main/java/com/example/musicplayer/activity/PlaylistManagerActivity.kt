package com.example.musicplayer.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.adapter.PlaylistAdapter
import com.example.musicplayer.data.PlaylistRepository
import com.example.musicplayer.databinding.ActivityPlaylistManagerBinding
import com.example.musicplayer.model.Playlist

class PlaylistManagerActivity : ComponentActivity() {
    private lateinit var binding: ActivityPlaylistManagerBinding
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var playlistRepository: PlaylistRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaylistManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        playlistRepository = PlaylistRepository(this)
        
        initViews()
        loadPlaylists()
    }
    
    private fun initViews() {
        // 初始化 RecyclerView
        playlistAdapter = PlaylistAdapter { playlist ->
            // 处理播放列表点击事件
            onPlaylistClick(playlist)
        }
        
        binding.playlistRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PlaylistManagerActivity)
            adapter = playlistAdapter
        }
        
        // 添加新播放列表按钮点击事件
        binding.addPlaylistButton.setOnClickListener {
            showCreatePlaylistDialog()
        }
    }
    
    private fun loadPlaylists() {
        val playlists = playlistRepository.getAllPlaylists()
        playlistAdapter.submitList(playlists)
    }
    
    private fun onPlaylistClick(playlist: Playlist) {
        // TODO: 实现播放列表点击事件
        // 可以打开播放列表详情页面
    }
    
    private fun showCreatePlaylistDialog() {
        // TODO: 实现创建播放列表对话框
    }
} 