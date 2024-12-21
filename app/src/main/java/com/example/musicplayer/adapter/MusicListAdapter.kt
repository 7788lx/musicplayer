package com.example.musicplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.musicplayer.model.MusicFile

class MusicListAdapter(
    private val context: Context,
    private var musicList: List<MusicFile> = emptyList()
) : BaseAdapter() {

    override fun getCount(): Int = musicList.size

    override fun getItem(position: Int): MusicFile = musicList[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false).apply {
                tag = ViewHolder(findViewById(android.R.id.text1))
            }
        
        val holder = view.tag as ViewHolder
        holder.textView.text = musicList[position].name
        
        return view
    }

    fun updateData(newList: List<MusicFile>) {
        musicList = newList
        notifyDataSetChanged()
    }

    private class ViewHolder(val textView: TextView)
} 