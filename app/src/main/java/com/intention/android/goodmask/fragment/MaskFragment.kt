package com.intention.android.goodmask.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.intention.android.goodmask.R
import com.intention.android.goodmask.databinding.FragMaskBinding

class MaskFragment : Fragment() {
    private var _binding: FragMaskBinding? = null
    private val binding get() = _binding!!
    lateinit var maskFanPower : SeekBar
    lateinit var maskFanPowerText : TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragMaskBinding.inflate(inflater, container, false)
        val view = binding.root

        maskFanPower = binding.seekBar
        maskFanPowerText = binding.fanTitle

        maskFanPower.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                maskFanPowerText.text = "팬 세기\n" + p1.toString()
            }
            override fun onStartTrackingTouch(p0: SeekBar?) {}
            override fun onStopTrackingTouch(p0: SeekBar?) {}
        })

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}