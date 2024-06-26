package com.dada.puretimer

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dada.puretimer.databinding.ActivityScoreBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoreBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var databaseReference: DatabaseReference
    private var mAuth: FirebaseAuth? = null

    // 오프라인에서도 데이터저장 가능하게 함
    private fun enablePersistence() {
        Firebase.database.setPersistenceEnabled(true)
    }

    private fun addOnBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }

        this.onBackPressedDispatcher.addCallback(this, callback)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isNetworkAvailable(this)){
            showAlertDialog()
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_score)
        auth = Firebase.auth
        database = Firebase.database.reference

        val user = FirebaseAuth.getInstance().currentUser // 로그인한 유저의 정보 가져오기
        val uid = user?.uid // 로그인한 유저의 고유 uid 가져오기
        databaseReference = uid?.let { FirebaseDatabase.getInstance().reference.child(it) }!!



        binding.homeButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }

        binding.joinCancleBtn.setOnClickListener {

            showJoinoutDialog()
        }

        binding.allResetBtn.setOnClickListener {
            mAuth = FirebaseAuth.getInstance()
            val userId = mAuth!!.currentUser?.uid
            if (userId != null) {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("모든 과목과 시간이 삭제됩니다.")
                builder.setPositiveButton("예") { dialogInterface: DialogInterface, i: Int ->
                    // 해당 유저의 데이터를 Realtime Database에서 삭제
                    val database = FirebaseDatabase.getInstance()
                    val userRef = database.getReference("users").child(uid)
                    userRef.removeValue()
                }

                builder.setNegativeButton("아니요") { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }

                builder.show()

            }
        }


        val rv_board = findViewById<RecyclerView>(R.id.rv)

        val itemList = ArrayList<BoardItem>()

        val database = FirebaseDatabase.getInstance()
        val myRef = uid?.let {
            database.getReference("users").child(it)
        }
        val boardAdapter = BoardAdapter(itemList)

        val childEventListener = object : ChildEventListener {
            var totalTime = 0
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val sub = dataSnapshot.child("sub").getValue(String::class.java)
                val time = dataSnapshot.child("timeTotal").getValue(String::class.java)
                if (sub != null && time != null) {
                    val boardItem = BoardItem(sub, time)
                    itemList.add(boardItem)
                    val parts = time.split(":")
                    val hours = parts[0].toInt()
                    val minutes = parts[1].toInt()
                    val seconds = parts[2].toInt()
                    totalTime += hours * 3600 + minutes * 60 + seconds
                    binding.totalTime.text = formatTime(totalTime)
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
    private fun formatTime(totalTimeInSeconds: Int): String {
        val hours = totalTimeInSeconds / 3600
        val minutes = (totalTimeInSeconds % 3600) / 60
        val seconds = totalTimeInSeconds % 60

        return String.format("%02d:%02d:%02d", hours,minutes, seconds)
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw      = connectivityManager.activeNetwork ?: return false
            val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                //for other device how are able to connect with Ethernet
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                //for check internet over Bluetooth
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
                else -> false
            }
        } else {
            return connectivityManager.activeNetworkInfo?.isConnected ?: false
        }
    }

    private fun showAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder
            .setMessage("인터넷 연결을 확인해주세요.\n데이터 누락이 발생할 수 있습니다.")
            .setPositiveButton("확인") { dialog, which ->

            }
            .setCancelable(false)
            .show()
    }

    private fun showJoinoutDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("탈퇴하시겠습니까?\n(기록과 회원정보가 모두 삭제됩니다.)")

        builder.setPositiveButton("예") { dialogInterface: DialogInterface, i: Int ->

            mAuth = FirebaseAuth.getInstance()
            val userId = mAuth!!.currentUser?.uid
            if (userId != null) {
                // 해당 유저의 데이터를 Realtime Database에서 삭제
                val database = FirebaseDatabase.getInstance()
                val userRef = database.getReference("users").child(userId)
                userRef.removeValue()

                mAuth!!.currentUser?.delete()
                Firebase.auth.signOut()
            }
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }

        builder.setNegativeButton("아니요") { dialogInterface: DialogInterface, i: Int ->
            // 아무런 작업을 수행하지 않음
        }

        // 다이얼로그를 표시
        builder.show()
    }
}