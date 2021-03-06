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

    // ?????? ?????????
    private fun getDailyTime(db: MaskDB) {
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        val r = Runnable {
            // ?????? ????????? ???????
            dayTime = db.MaskDao().getTime(day.toString()) / (1000 * 60)
            val dayTimeInfo: String
            if (dayTime >= 60)
                dayTimeInfo = "${dayTime / 60}?????? ${dayTime % 60}???"
            else
                dayTimeInfo = "$dayTime ???"

            dayUseTV.text = dayTimeInfo
            Log.e("DailyTime!", dayTime.toString())
        }
        val thread = Thread(r)
        thread.start()
    }

    // ?????? ?????????, ??? ?????????
    private fun getWeeklyTime(db: MaskDB) {
        // ??? ~ ??? ???
        var totalTime:Long = 0
        val r = Runnable {
            for (i in 1..7) {
                timeByDay[i - 1] = db.MaskDao().getTime(i.toString())
                totalTime += timeByDay[i - 1]
            }
            // ??? ????????? ??????????????? ????????? ??????
            totalTimeMin = totalTime / (1000 * 60)
            Log.e("Total Time", totalTimeMin.toString())

            val totalTimeInfo: String
            if (totalTimeMin >= 60)
                totalTimeInfo = "${totalTimeMin / 60}?????? ${totalTimeMin % 60}???"
            else
                totalTimeInfo = "$totalTimeMin ???"

            totalUseTV.text = totalTimeInfo

            // ????????? ?????? ????????? ????????? ????????? ?????????
            for (i in 0..6) {
                timeByDayMin[i] = timeByDay[i] / (1000 * 60)
            }
            // ????????? ??????????????? ??????
            for (i in 0..6) {
                Log.e("LIST!", timeByDay[i].toString() + " ")
            }
            // ????????? ??? ??????
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

        /*dayUseTV.text = dayUseTime.toString() + "??????"
        totalUseTV.text = monthUseTime.toString() + "??????"*/
        weekUseTV.text = weekUseTime.toString() + "??????"

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

            axisLeft.run { //?????? ???. ??? Y?????? ?????? ?????????.
                axisMaximum = 24f //100 ????????? ?????? ????????? ?????? 101f??? ???????????? ??????
                axisMinimum = 0f // ????????? 0
                granularity = 12f // 50 ???????????? ?????? ???????????? ??????.
                setDrawLabels(true) // ??? ????????? ?????? (0, 50, 100)
                setDrawGridLines(true) //?????? ?????? ??????
                setDrawAxisLine(false) // ??? ????????? ??????
                axisLineColor = ContextCompat.getColor(context, R.color.gray) // ??? ?????? ??????
                gridColor = ContextCompat.getColor(context, R.color.gray) // ??? ?????? ?????? ?????? ??????
                textColor = ContextCompat.getColor(context, R.color.gray) // ?????? ????????? ?????? ??????
                textSize = 12f //?????? ????????? ??????
            }

            setTouchEnabled(true)

            xAxis.run {
                position = XAxis.XAxisPosition.BOTTOM //X?????? ??????????????? ??????.
                granularity = 1f // 1 ???????????? ?????? ??????
                setDrawAxisLine(true) // ??? ??????
                setDrawGridLines(true) // ??????
                textColor = ContextCompat.getColor(context, R.color.gray) //?????? ??????
                axisLineColor = ContextCompat.getColor(context, R.color.gray) // ??? ?????? ??????
                gridColor = ContextCompat.getColor(context, R.color.gray) // ??? ?????? ?????? ?????? ??????
                textSize = 10f // ????????? ??????
                valueFormatter = UsageXAxisFormatter() // X??? ?????????(?????? ???????????? ??????) ???????????? ?????? ??????
            }
            axisRight.isEnabled = false
            animateY(1000)
            legend.isEnabled = false

            val set = BarDataSet(entries, "DataSet")
                .apply {
                    setDrawValues(true)
                    color = ContextCompat.getColor(view?.context!!, R.color.gmcolor) // ??? ????????? ??? ??????
                }

            val dataset = BarData(set)
            dataset.setDrawValues(true)
            dataset.barWidth = 0.5f //?????? ?????? ??????
            data = dataset
            data.setDrawValues(true)
            setFitBars(true)
            invalidate()
        }
    }


    class UsageXAxisFormatter : ValueFormatter() {
        private val days = arrayOf("???", "???", "???", "???", "???", "???", "???")
        override fun getAxisLabel(value: Float, axis: AxisBase?): String {
            return days.getOrNull(value.toInt() - 1) ?: value.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}