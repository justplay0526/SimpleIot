package com.practice.simpleiot

data class SummaryItem(
    val id: String = "",
    val timestamp: Long = 0,
    val totalFrames: Int = 0,
    val postures: Map<String, PostureDetail> = emptyMap()
)

data class PostureDetail(
    val frames: Int = 0,
    val duration_sec: Double = 0.0
)
