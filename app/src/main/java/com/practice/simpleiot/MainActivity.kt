package com.practice.simpleiot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
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

                recyclerView.adapter = SummaryAdapter(summaryList)
            }

            override fun onCancelled(error: DatabaseError) {
                // handle error
            }
        })
    }
}