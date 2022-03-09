package com.intention.android.goodmask.util

import android.opengl.ETC1.getHeight

import android.opengl.ETC1.getWidth

import com.github.mikephil.charting.highlight.Highlight
import android.content.Context
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View

import android.widget.TextView

import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.intention.android.goodmask.R


class CustomMarkerView : MarkerView
{
    private lateinit var tvContent: TextView // marker
    constructor(context: Context?, layoutResource: Int) : super(context, layoutResource) {
        tvContent = findViewById(R.id.marker)
    }

    // draw override를 사용해 marker의 위치 조정 (bar의 상단 중앙)
    override fun draw(canvas: Canvas?) {
        canvas!!.translate(-(width / 2).toFloat(), -(height.toFloat()+10) )
        super.draw(canvas)
    }

    // entry를 content의 텍스트에 지정
   override fun refreshContent(e: Entry?, highlight: Highlight?) {
       tvContent.text = e?.y?.toInt().toString() + "시간"
       super.refreshContent(e, highlight)
   }
}