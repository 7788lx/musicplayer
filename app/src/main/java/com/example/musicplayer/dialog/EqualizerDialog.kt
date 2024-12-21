package com.example.musicplayer.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.example.musicplayer.databinding.DialogEqualizerBinding

class EqualizerDialog(
    context: Context,
    private val onPresetSelected: (Int) -> Unit,
    private val onBandChanged: (Int, Int) -> Unit
) : Dialog(context) {

    private lateinit var binding: DialogEqualizerBinding
    private val presets = listOf(
        "Normal",
        "Classical",
        "Dance",
        "Folk",
        "Heavy Metal",
        "Hip Hop",
        "Jazz",
        "Pop",
        "Rock"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogEqualizerBinding.inflate(LayoutInflater.from(context))
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        // 设置预设选择器
        val presetAdapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            presets
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.presetSpinner.adapter = presetAdapter
        binding.presetSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                onPresetSelected(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        // 设置频段调节器
        binding.band1Slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) onBandChanged(0, value.toInt())
        }
        binding.band2Slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) onBandChanged(1, value.toInt())
        }
        binding.band3Slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) onBandChanged(2, value.toInt())
        }
        binding.band4Slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) onBandChanged(3, value.toInt())
        }
        binding.band5Slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) onBandChanged(4, value.toInt())
        }
    }
} 