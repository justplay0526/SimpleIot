package com.practice.simpleiot

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SummaryAdapter(private val data: List<SummaryItem>) :
    RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    private val labelMap = mapOf(
        "Upright Head" to "坐姿良好",
        "Head Down" to "低頭",
        "Leaning Sideways" to "身體側彎",
        "Relaxed Posture" to "姿勢鬆散",
        "Detection Error" to "偵測錯誤")
    private val greenSet = setOf("Upright Head")
    private val redSet = setOf("Head Down", "Leaning Sideways")
    private val blackSet = setOf("Relaxed Posture", "Detection Error")
    private val evaluationText = listOf("資料不足，無法評估", "姿勢良好，請繼續保持"
         , "嚴重低頭，請改善坐姿", "嚴重側彎，請改善坐姿", "姿勢需注意，請改善坐姿", "姿勢普通，仍有進步空間")

    class SummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.timestampText)
        val greenText: TextView = view.findViewById(R.id.GreenText)
        val redText: TextView = view.findViewById(R.id.RedText)
        val blackText: TextView = view.findViewById(R.id.BlackText)
        val evaluationText: TextView = view.findViewById(R.id.evaluationText)
        val pieChart: PieChart = view.findViewById(R.id.pieChart)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val item = data[position]

        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault())
        holder.timeText.text = sdf.format(Date(item.timestamp * 1000))

        val greenList = mutableListOf<String>()
        val redList = mutableListOf<String>()
        val blackList = mutableListOf<String>()

        item.postures.forEach { (key, value) ->
            val label = labelMap[key] ?: key
            val text = "$label：${"%.1f".format(value.duration_sec)} 秒"

            when (key) {
                in greenSet -> greenList.add(text)
                in redSet -> redList.add(text)
                in blackSet -> blackList.add(text)
            }
        }
        blackList.reverse()

        val totalDuration = (item.postures["Head Down"]?.duration_sec ?: 0.0) +
                (item.postures["Leaning Sideways"]?.duration_sec ?: 0.0) +
                (item.postures["Upright Head"]?.duration_sec ?: 0.0) +
                (item.postures["Relaxed Posture"]?.duration_sec ?: 0.0)

        val uprightDuration = item.postures["Upright Head"]?.duration_sec ?: 0.0
        val headDownDuration = (item.postures["Head Down"]?.duration_sec ?: 0.0)
        val leaningDuration =(item.postures["Leaning Sideways"]?.duration_sec ?: 0.0)
        val redDuration = (item.postures["Head Down"]?.duration_sec ?: 0.0) +
                (item.postures["Leaning Sideways"]?.duration_sec ?: 0.0)

        val evaluation = when {
            totalDuration < 30 -> 0
            uprightDuration / totalDuration > 0.5 -> 1
            headDownDuration / totalDuration > 0.3 -> 2
            leaningDuration / totalDuration > 0.3 -> 3
            redDuration / totalDuration > 0.6 -> 4
            else -> 5
        }

        val evaluationColorMap = mapOf(
            0 to "#9E9E9E".toColorInt(), // 灰
            1 to "#4CAF50".toColorInt(), // 綠
            2 to "#F44336".toColorInt(), // 紅
            3 to "#F44336".toColorInt(), // 紅
            4 to "#F44336".toColorInt(), // 紅
            5 to "#9E9E9E".toColorInt() // 灰
        )

        holder.greenText.text = greenList.joinToString("\n")
        holder.redText.text = redList.joinToString("\n")
        holder.blackText.text = blackList.joinToString("\n")
        holder.evaluationText.text = evaluationText[evaluation]
        holder.evaluationText.setTextColor(evaluationColorMap[evaluation] ?: "#000000".toColorInt())

        // 將姿勢統計資料轉為 PieEntry 清單
        val entries = item.postures.mapNotNull { (key, value) ->
            if (key == "Detection Error" || value.duration_sec == 0.0) return@mapNotNull null
            PieEntry(value.duration_sec.toFloat(), labelMap[key] ?: key).also {
                it.data = key // 儲存原始 key 用於對應顏色
            }
        }

        if (entries.isEmpty()) {
            holder.pieChart.clear()
            holder.pieChart.setNoDataText("無有效姿勢資料")
            return
        }

        val colorMap = mapOf(
            "Upright Head" to "#4CAF50".toColorInt(), // 綠
            "Head Down" to "#F44336".toColorInt(),    // 紅
            "Leaning Sideways" to "#FF9800".toColorInt(), // 橘
            "Relaxed Posture" to "#9E9E9E".toColorInt()  // 灰
        )

        val colors = entries.mapNotNull { entry ->
            val key = entry.data as? String
            colorMap[key]
        }
        // 建立資料集合
        val dataSet = PieDataSet(entries, "姿勢時間分布").apply {
            setColors(colors)
            valueTextSize = 12f
            valueTextColor = android.graphics.Color.BLACK
        }

        // 設定圖表資料
        holder.pieChart.data = PieData(dataSet)
        holder.pieChart.description.isEnabled = false
        holder.pieChart.setDrawEntryLabels(false)
        holder.pieChart.legend.isEnabled = false
        holder.pieChart.invalidate() // 重繪圖表
    }

    override fun getItemCount(): Int = data.size
}
