package com.example.musicplayer.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.R
import com.example.musicplayer.databinding.ItemMusicBinding
import com.example.musicplayer.model.MusicFile

class MusicAdapter(
    private val onItemClick: (MusicFile) -> Unit
) : ListAdapter<MusicFile, MusicAdapter.ViewHolder>(MusicDiffCallback()) {

    var currentPlayingIndex = -1
        set(value) {
            val oldValue = field
            field = value
            if (oldValue != -1) {
                notifyItemChanged(oldValue, PAYLOAD_PLAYING_STATE)
            }
            if (value != -1) {
                notifyItemChanged(value, PAYLOAD_PLAYING_STATE)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMusicBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = getItem(position)
        holder.bind(music)
        
        holder.itemView.setBackgroundResource(
            if (position == currentPlayingIndex) 
                R.drawable.bg_playing_item
            else 
                android.R.color.transparent
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: List<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.itemView.setBackgroundResource(
                if (position == currentPlayingIndex) 
                    R.drawable.bg_playing_item
                else 
                    android.R.color.transparent
            )
        }
    }

    inner class ViewHolder(
        private val binding: ItemMusicBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { position ->
                    onItemClick(getItem(position))
                }
            }
        }

        fun bind(music: MusicFile) {
            binding.musicName.text = music.name
        }
    }

    class MusicDiffCallback : DiffUtil.ItemCallback<MusicFile>() {
        override fun areItemsTheSame(oldItem: MusicFile, newItem: MusicFile): Boolean {
            return oldItem.path == newItem.path
        }

        override fun areContentsTheSame(oldItem: MusicFile, newItem: MusicFile): Boolean {
            return oldItem == newItem
        }
    }

    companion object {
        private const val PAYLOAD_PLAYING_STATE = "playing_state"
    }
} 