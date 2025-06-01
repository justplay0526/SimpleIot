package com.practice.simpleiot

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SummaryAdapter(private val data: List<SummaryItem>) :
    RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    private val labelMap = mapOf(
        "Upright Head" to "坐姿良好",
        "Head Down" to "低頭",
        "Leaning Sideways" to "身體傾斜",
        "Relaxed Posture" to "姿勢鬆散",
        "Detection Error" to "偵測錯誤")
    private val greenSet = setOf("Upright Head")
    private val redSet = setOf("Head Down", "Leaning Sideways")
    private val blackSet = setOf("Relaxed Posture", "Detection Error")

    class SummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.timestampText)
        val greenText: TextView = view.findViewById(R.id.GreenText)
        val redText: TextView = view.findViewById(R.id.RedText)
        val blackText: TextView = view.findViewById(R.id.BlackText)
        val evaluationText: TextView = view.findViewById(R.id.evaluationText)

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

        val totalDuration = item.postures.values.sumOf { it.duration_sec }
        val uprightDuration = item.postures["Upright Head"]?.duration_sec ?: 0.0
        val redDuration = (item.postures["Head Down"]?.duration_sec ?: 0.0) +
                (item.postures["Leaning Sideways"]?.duration_sec ?: 0.0)

        val evaluation = when {
            totalDuration < 10 -> "資料不足，無法評估"
            uprightDuration / totalDuration > 0.6 -> "姿勢良好，請繼續保持"
            redDuration / totalDuration > 0.4 -> "姿勢需注意，請改善坐姿"
            else -> "姿勢普通，仍有進步空間"
        }

        holder.greenText.text = greenList.joinToString("\n")
        holder.redText.text = redList.joinToString("\n")
        holder.blackText.text = blackList.joinToString("\n")
        holder.evaluationText.text = evaluation

    }

    override fun getItemCount(): Int = data.size
}
