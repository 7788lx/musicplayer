package com.example.musicplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.musicplayer.databinding.ItemSleepTimerBinding
import com.example.musicplayer.model.SleepTimer

class SleepTimerAdapter(
    private val onItemClick: (SleepTimer) -> Unit
) : ListAdapter<SleepTimer, SleepTimerAdapter.ViewHolder>(SleepTimerDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSleepTimerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemSleepTimerBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                onItemClick(getItem(adapterPosition))
            }
        }

        fun bind(timer: SleepTimer) {
            binding.timerText.text = timer.description
        }
    }

    class SleepTimerDiffCallback : DiffUtil.ItemCallback<SleepTimer>() {
        override fun areItemsTheSame(oldItem: SleepTimer, newItem: SleepTimer): Boolean {
            return oldItem.minutes == newItem.minutes
        }

        override fun areContentsTheSame(oldItem: SleepTimer, newItem: SleepTimer): Boolean {
            return oldItem == newItem
        }
    }
} 