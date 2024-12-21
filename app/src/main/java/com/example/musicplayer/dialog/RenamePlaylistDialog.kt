package com.example.musicplayer.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog

class RenamePlaylistDialog(
    context: Context,
    private val currentName: String,
    private val onConfirm: (String) -> Unit
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val editText = EditText(context).apply {
            setText(currentName)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(50, 30, 50, 30)
            }
        }
        
        AlertDialog.Builder(context)
            .setTitle("重命名播放列表")
            .setView(editText)
            .setPositiveButton("确定") { _, _ ->
                val newName = editText.text.toString()
                if (newName.isNotBlank()) {
                    onConfirm(newName)
                }
            }
            .setNegativeButton("取消", null)
            .create()
            .show()
            
        dismiss()
    }
} 