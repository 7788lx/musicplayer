package com.example.musicplayer.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import com.example.musicplayer.R

class CreatePlaylistDialog(
    context: Context,
    private val onConfirm: (String) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val editText = EditText(context).apply {
            hint = "播放列表名称"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(50, 30, 50, 30)
            }
        }
        
        AlertDialog.Builder(context)
            .setTitle("新建播放列表")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val name = editText.text.toString()
                if (name.isNotBlank()) {
                    onConfirm(name)
                }
            }
            .setNegativeButton("取消", null)
            .create()
            .show()
            
        dismiss()
    }
} 