package com.intention.android.goodmask.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.intention.android.goodmask.R
import com.intention.android.goodmask.databinding.FragStaticsBinding

class StaticsFragment : Fragment() {
    private var _binding: FragStaticsBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragStaticsBinding.inflate(inflater, container, false)
        val view = binding.root



        var uChart : BarChart = binding.usageChart

        makeChart(uChart, view)

        return view
    }

    fun makeChart(uChart : BarChart, view: View){
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1.0f,3.0f))
        entries.add(BarEntry(2.0f,5.0f))
        entries.add(BarEntry(3.0f,7.0f))
        entries.add(BarEntry(4.0f,6.0f))
        entries.add(BarEntry(5.0f,9.0f))
        entries.add(BarEntry(6.0f,1.0f))
        entries.add(BarEntry(7.0f,10.0f))

        uChart.run {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener{
                override fun onValueSelected(e: Entry?, h: Highlight?) {

                }

                override fun onNothingSelected() {
                    TODO("Not yet implemented")
                }

            })
            description.isEnabled = false
            valuesToHighlight()
            setMaxVisibleValueCount(7)
            setPinchZoom(false)
            setDrawBarShadow(false)
            setDrawGridBackground(false)
            setTouchEnabled(false)

            axisLeft.run { //왼쪽 축. 즉 Y방향 축을 뜻한다.
                axisMaximum = 24f //100 위치에 선을 그리기 위해 101f로 맥시멈값 설정
                axisMinimum = 0f // 최소값 0
                granularity = 12f // 50 단위마다 선을 그리려고 설정.
                setDrawLabels(true) // 값 적는거 허용 (0, 50, 100)
                setDrawGridLines(true) //격자 라인 활용
                setDrawAxisLine(false) // 축 그리기 설정
                axisLineColor = ContextCompat.getColor(context,R.color.gray) // 축 색깔 설정
                gridColor = ContextCompat.getColor(context,R.color.gray) // 축 아닌 격자 색깔 설정
                textColor = ContextCompat.getColor(context,R.color.gray) // 라벨 텍스트 컬러 설정
                textSize = 12f //라벨 텍스트 크기
            }

            setTouchEnabled(true)

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM //X축을 아래에다가 둔다.
                granularity = 1f // 1 단위만큼 간격 두기
                setDrawAxisLine(true) // 축 그림
                setDrawGridLines(true) // 격자
                textColor = ContextCompat.getColor(context,R.color.gray) //라벨 색상
                axisLineColor = ContextCompat.getColor(context,R.color.gray) // 축 색깔 설정
                gridColor = ContextCompat.getColor(context,R.color.gray) // 축 아닌 격자 색깔 설정
                textSize = 10f // 텍스트 크기
                valueFormatter = UsageXAxisFormatter() // X축 라벨값(밑에 표시되는 글자) 바꿔주기 위해 설정
            }
            axisRight.isEnabled = false
            animateY(1000)
            legend.isEnabled = false

            val set = BarDataSet(entries,"DataSet")
                .apply {
                    setDrawValues(true)
                    color = ContextCompat.getColor(view?.context!!,R.color.gmcolor) // 바 그래프 색 설정
                }

            val dataset = BarData(set)
            dataset.setDrawValues(true)
            dataset.barWidth = 0.5f //막대 너비 설정
            data = dataset
            data.setDrawValues(true)
            setFitBars(true)
            invalidate()
        }
    }


    class UsageXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("월","화","수","목","금","토","일")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt()-1) ?: value.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}