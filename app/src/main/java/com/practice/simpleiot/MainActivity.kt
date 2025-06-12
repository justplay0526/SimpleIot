package com.practice.simpleiot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        refreshData(recyclerView, swipeRefreshLayout = null)
        swipeRefreshLayout.setOnRefreshListener {
            refreshData(recyclerView, swipeRefreshLayout)
        }
    }

    private fun refreshData(
        recyclerView: RecyclerView,
        swipeRefreshLayout: SwipeRefreshLayout? = null
    ) {
        val summaryRef = FirebaseDatabase.getInstance().getReference("summary")
        val summaryList = mutableListOf<SummaryItem>()

        summaryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                summaryList.clear()
                for (summarySnap in snapshot.children) {
                    val id = summarySnap.key ?: continue
                    val timestamp = summarySnap.child("timestamp").getValue(Long::class.java) ?: 0
                    val totalFrames = summarySnap.child("total_frames").getValue(Int::class.java) ?: 0

                    val postureMap = mutableMapOf<String, PostureDetail>()
                    for (postureSnap in summarySnap.children) {
                        val key = postureSnap.key ?: continue
                        if (key == "timestamp" || key == "total_frames") continue

                        val frames = postureSnap.child("frames").getValue(Int::class.java) ?: 0
                        val duration = postureSnap.child("duration_sec").getValue(Double::class.java) ?: 0.0
                        postureMap[key] = PostureDetail(frames, duration)
                    }

                    summaryList.add(SummaryItem(id, timestamp, totalFrames, postureMap))
                }

                recyclerView.adapter = SummaryAdapter(summaryList.reversed())
                swipeRefreshLayout?.isRefreshing = false // 停止刷新動畫
            }

            override fun onCancelled(error: DatabaseError) {
                swipeRefreshLayout?.isRefreshing = false
                // handle error if needed
            }
        })
    }

}