package com.example.musicplayer.model

import java.io.File
import android.media.MediaMetadataRetriever

data class MusicFile(
    val path: String,
    val name: String,
    val artist: String = "未知歌手"
) {
    companion object {
        private val retriever = MediaMetadataRetriever()
        private val cache = mutableMapOf<String, MusicFile>()

        fun fromPath(path: String): MusicFile? {
            cache[path]?.let { return it }

            return try {
                val file = File(path)
                if (file.exists()) {
                    val nameWithoutExtension = file.nameWithoutExtension
                    retriever.setDataSource(path)
                    val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "未知歌手"
                    MusicFile(path, nameWithoutExtension, artist).also {
                        cache[path] = it
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        fun clearCache() {
            cache.clear()
        }
    }
} 