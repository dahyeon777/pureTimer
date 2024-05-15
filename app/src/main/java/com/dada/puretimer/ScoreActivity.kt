package com.dada.puretimer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dada.puretimer.databinding.ActivityScoreBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoreBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_score)
        auth = Firebase.auth
        database = Firebase.database.reference

        val user = FirebaseAuth.getInstance().currentUser // 로그인한 유저의 정보 가져오기
        val uid = user?.uid // 로그인한 유저의 고유 uid 가져오기

        binding.homeButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        val rv_board = findViewById<RecyclerView>(R.id.rv)

        val itemList = ArrayList<BoardItem>()

        val database = FirebaseDatabase.getInstance()
        val myRef = uid?.let {
            database.getReference("users").child(it)
                .child("과목,시간기록")
        }
        val boardAdapter = BoardAdapter(itemList)

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val sub = dataSnapshot.child("sub").getValue(String::class.java)
                val time = dataSnapshot.child("time").getValue(String::class.java)
                if (sub != null && time != null) {
                    val boardItem = BoardItem(sub, time)
                    itemList.add(boardItem)
                    boardAdapter.notifyDataSetChanged() // 어댑터에 변경 알림
                } else {
                    Log.d("subtime", "데이터 안들어옴")
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // 데이터가 변경될 때 호출됩니다.
                // 여기서 필요하면 해당 데이터 항목을 업데이트할 수 있습니다.
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                // 데이터가 삭제될 때 호출됩니다.
                // 여기서 필요하면 해당 데이터 항목을 삭제할 수 있습니다.
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                // 데이터의 위치가 변경될 때 호출됩니다. (일반적으로 사용되지 않음)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // 데이터 가져오기가 취소되었거나 실패한 경우의 처리
                Log.e("sumtime3", "Database operation canceled: ${databaseError.message}")
            }
        }

        if (myRef != null) {
            myRef.addChildEventListener(childEventListener)
        }

        rv_board.adapter = boardAdapter
        rv_board.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        boardAdapter.notifyDataSetChanged()

    }
}