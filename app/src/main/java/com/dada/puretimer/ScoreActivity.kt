package com.dada.puretimer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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

        binding.homeButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        val rv_board = findViewById<RecyclerView>(R.id.rv)

        val itemList = ArrayList<BoardItem>()

        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users").child("유저이름123")
            .child("과목,시간기록")
        val boardAdapter = BoardAdapter(itemList)

        val childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val sub = dataSnapshot.child("sub").getValue(String::class.java)
                val time = dataSnapshot.child("time").getValue(String::class.java)
                if (sub != null && time != null) {
                    Log.d("subtime5", "데이터 들어오긴 함")
                    val boardItem = BoardItem(sub, time)
                    itemList.add(boardItem)
                    boardAdapter.notifyDataSetChanged() // 어댑터에 변경 알림
                } else {
                    Log.d("subtime2", "데이터 안들어옴")
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

// ChildEventListener를 데이터베이스 참조에 연결
        myRef.addChildEventListener(childEventListener)



       /* // 데이터를 한 번만 읽어오는 경우
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    for (recordSnapshot in userSnapshot.child("과목,시간기록").children) {
                        val sub1 = recordSnapshot.child("sub").getValue(String::class.java)
                        val time1 = recordSnapshot.child("time").getValue(String::class.java)
                        if (sub1 != null && time1 != null) {
                            Log.d("subtime5", "데이터 들어오긴 함")
                            // DataModel 객체 생성
                            val dataModel = DataModel(sub1, time1)
                            // DataModel 객체에서 필요한 정보를 추출하여 BoardItem 객체 생성
                            itemList.add(BoardItem("수학","13:00"))
                            val boardItem = BoardItem(dataModel.sub, dataModel.time)
                            itemList.add(boardItem)
                        }
                        else{
                            Log.d("subtime2", "데이터 안들어옴")
                        }
                    }
                }
                for (item in itemList) {
                    Log.d("subtime", "Subject: ${item.sub}, Time: ${item.time}")
                }

                // 데이터 변경 후에 어댑터 설정
                val boardAdapter = BoardAdapter(itemList)
                rv_board.adapter = boardAdapter
                rv_board.layoutManager = LinearLayoutManager(this@ScoreActivity, LinearLayoutManager.VERTICAL, false)
                boardAdapter.notifyDataSetChanged()
            }



            override fun onCancelled(databaseError: DatabaseError) {
                itemList.add(BoardItem("영어","13:00"))
                // 데이터 읽기를 취소했거나 실패한 경우의 처리
                Log.e("sumtime3", "Database operation canceled: ${databaseError.message}")
            }
        })
*/

        rv_board.adapter = boardAdapter
        rv_board.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        boardAdapter.notifyDataSetChanged()

    }
}