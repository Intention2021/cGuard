package com.intention.android.goodmask.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.intention.android.goodmask.db.MaskDB
import com.intention.android.goodmask.util.CustomMarkerView
import kotlinx.coroutines.flow.asFlow
import java.util.*
import kotlin.collections.ArrayList

class StaticsFragment : Fragment() {
    private var _binding: FragStaticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var dayUseTV: TextView
    private lateinit var weekUseTV: TextView
    private lateinit var totalUseTV: TextView

    private var dayUseTime: Int = 0
    private var weekUseTime: Int = 0
    private var monthUseTime: Int = 0

    private var db: MaskDB? = null
    private var dayTime: Long = 0
    private var timeByDay = longArrayOf(0, 0, 0, 0, 0, 0, 0)
    private var timeByDayMin = longArrayOf(0, 0, 0, 0, 0, 0, 0)
    private var totalTimeMin: Long = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragStaticsBinding.inflate(inflater, container, false)
        val view = binding.root

        dayUseTV = binding.dayUsageTime
        weekUseTV = binding.weekUsageTime
        totalUseTV = binding.totalUsageTime

        var uChart: BarChart = binding.usageChart

        makeChart(uChart, view)

        db = MaskDB.getInstance(context?.applicationContext!!)
        getDailyTime(db!!)
        getWeeklyTime(db!!)

        return view
    }

    // 일일 사용량
    private fun getDailyTime(db: MaskDB) {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val r = Runnable {
            // 일일 사용량 표시?
            dayTime = db.MaskDao().getTime(day.toString()) / (1000 * 60)
            val dayTimeInfo: String
            if (dayTime >= 60)
                dayTimeInfo = "${dayTime / 60}시간 ${dayTime % 60}분"
            else
                dayTimeInfo = "$dayTime 분"

            dayUseTV.text = dayTimeInfo
            Log.e("DailyTime!", dayTime.toString())
        }
        val thread = Thread(r)
        thread.start()
    }

    // 주간 사용량, 총 사용량
    private fun getWeeklyTime(db: MaskDB) {
        // 일 ~ 토 순
        val totalTime:Long = db.MaskDao().getTotal()
        val r = Runnable {
            for (i in 1..7) {
                timeByDay[i - 1] = db.MaskDao().getTime(i.toString())
            }
            // 총 시간을 밀리초에서 분으로 변환
            totalTimeMin = totalTime / (1000 * 60)
            Log.e("Total Time", totalTimeMin.toString())

            val totalTimeInfo: String
            if (totalTimeMin >= 60)
                totalTimeInfo = "${totalTimeMin / 60}시간 ${totalTimeMin % 60}분"
            else
                totalTimeInfo = "$totalTimeMin 분"

            totalUseTV.text = totalTimeInfo

            // 요일별 시간 배열에 단위를 분으로 맞추기
            for (i in 0..6) {
                timeByDayMin[i] = timeByDay[i] / (1000 * 60)
            }
            // 요일별 밀리세컨드 배열
            for (i in 0..6) {
                Log.e("LIST!", timeByDay[i].toString() + " ")
            }
            // 요일별 분 배열
            for (i in 0..6) {
                Log.e("LIST MINUTE!", timeByDayMin[i].toString() + " ")
            }
        }
        val thread = Thread(r)
        thread.start()
    }

    fun makeChart(uChart: BarChart, view: View) {
        val currentDay = 3
        weekUseTime = 0
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(1.0f, timeByDayMin[0]/60.0f))
        entries.add(BarEntry(2.0f, timeByDayMin[1]/60.0f))
        entries.add(BarEntry(3.0f, timeByDayMin[2]/60.0f))
        entries.add(BarEntry(4.0f, timeByDayMin[3]/60.0f))
        entries.add(BarEntry(5.0f, timeByDayMin[4]/60.0f))
        entries.add(BarEntry(6.0f, timeByDayMin[5]/60.0f))
        entries.add(BarEntry(7.0f, timeByDayMin[6]/60.0f))

        dayUseTime = entries[currentDay].y.toInt()
        entries.forEach {
            weekUseTime += it.y.toInt()
        }
        weekUseTV.text = weekUseTime.toString()

        /*dayUseTV.text = dayUseTime.toString() + "시간"
        totalUseTV.text = monthUseTime.toString() + "시간"*/
        weekUseTV.text = weekUseTime.toString() + "시간"

        val cMarker = CustomMarkerView(context, layoutResource = R.layout.chart_marker)

        uChart.run {
            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                }

                override fun onNothingSelected() {
                }

            })
            marker = cMarker
            description.isEnabled = false
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
                axisLineColor = ContextCompat.getColor(context, R.color.gray) // 축 색깔 설정
                gridColor = ContextCompat.getColor(context, R.color.gray) // 축 아닌 격자 색깔 설정
                textColor = ContextCompat.getColor(context, R.color.gray) // 라벨 텍스트 컬러 설정
                textSize = 12f //라벨 텍스트 크기
            }

            setTouchEnabled(true)

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM //X축을 아래에다가 둔다.
                granularity = 1f // 1 단위만큼 간격 두기
                setDrawAxisLine(true) // 축 그림
                setDrawGridLines(true) // 격자
                textColor = ContextCompat.getColor(context, R.color.gray) //라벨 색상
                axisLineColor = ContextCompat.getColor(context, R.color.gray) // 축 색깔 설정
                gridColor = ContextCompat.getColor(context, R.color.gray) // 축 아닌 격자 색깔 설정
                textSize = 10f // 텍스트 크기
                valueFormatter = UsageXAxisFormatter() // X축 라벨값(밑에 표시되는 글자) 바꿔주기 위해 설정
            }
            axisRight.isEnabled = false
            animateY(1000)
            legend.isEnabled = false

            val set = BarDataSet(entries, "DataSet")
                .apply {
                    setDrawValues(true)
                    color = ContextCompat.getColor(view?.context!!, R.color.gmcolor) // 바 그래프 색 설정
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
        private val days = arrayOf("월", "화", "수", "목", "금", "토", "일")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt() - 1) ?: value.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}