package com.dada.puretimer

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dada.puretimer.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.concurrent.timer


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var time = 0
    private var timerTask: Timer? = null
    private var buttonPressCount = 0
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference


    private lateinit var subArray: MutableList<String>
    private lateinit var sharedPref: SharedPreferences

    // 오프라인에서도 데이터저장 가능하게 함
    private fun enablePersistence() {
        Firebase.database.setPersistenceEnabled(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        auth = Firebase.auth
        database = Firebase.database.reference




        val user = FirebaseAuth.getInstance().currentUser // 로그인한 유저의 정보 가져오기
        val uid = user?.uid // 로그인한 유저의 고유 uid 가져오기
        Toast.makeText(this,uid,Toast.LENGTH_SHORT).show()
        var subChange = 0




        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // 저장된 데이터를 로드합니다.
        val savedColors =
            sharedPref.getStringSet("colorArray", HashSet<String>())?.toList() ?: emptyList()
        subArray = savedColors.toMutableList()


        binding.subTextBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val input = EditText(this) // EditText를 생성

            builder.setTitle("과목을 선택하세요.")
                .setItems(subArray.toTypedArray()) { dialog, which ->
                    // 여기서 인자 'which'는 배열의 position을 나타냅니다.
                    binding.subTextBtn.text = subArray[which]
                    subChange=1

                    resetTimer()//과목이 변경됨에 따라 시간 초기화
                }
                // "추가" 버튼 추가
                .setPositiveButton("추가") { dialog, which ->
                    val newColor = input.text.toString() // 입력된 텍스트 가져오기
                    if (newColor.isNotEmpty()) {
                        // 중복 체크
                        val isDuplicate = subArray.contains(newColor)
                        if (!isDuplicate) {
                            subArray.add(newColor) // colorArray에 새로운 항목 추가
                            // 저장된 데이터를 SharedPreferences에 업데이트합니다.
                            sharedPref.edit().putStringSet("colorArray", subArray.toSet()).apply()
                            // 항목을 추가한 직후에 다이얼로그의 리스트를 업데이트하여 새로운 항목이 표시되도록 함
                            (dialog as AlertDialog).listView.adapter =
                                ArrayAdapter<String>(
                                    this,
                                    android.R.layout.simple_list_item_1,
                                    subArray
                                )
                        } else {
                            // 중복된 경우 토스트 메시지 출력
                            Toast.makeText(this, "이미 존재하는 항목입니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("취소", null) // "취소" 버튼 추가
                .setView(input) // 다이얼로그에 EditText 추가

            // 다이얼로그를 띄워주기
            val dialog = builder.create()
            dialog.show()

            // "추가" 버튼을 클릭하면 다이얼로그가 닫히지 않고 새 항목이 추가됩니다.
            dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setOnClickListener {
                val newColor = input.text.toString() // 입력된 텍스트 가져오기
                if (newColor.isNotEmpty()) {
                    // 중복 체크
                    val isDuplicate = subArray.contains(newColor)
                    if (!isDuplicate) {
                        subArray.add(newColor) // colorArray에 새로운 항목 추가
                        // 저장된 데이터를 SharedPreferences에 업데이트합니다.
                        sharedPref.edit().putStringSet("colorArray", subArray.toSet()).apply()
                        // 항목을 추가한 직후에 다이얼로그의 리스트를 업데이트하여 새로운 항목이 표시되도록 함
                        (dialog as AlertDialog).listView.adapter =
                            ArrayAdapter<String>(
                                this,
                                android.R.layout.simple_list_item_1,
                                subArray
                            )
                    } else {
                        // 중복된 경우 토스트 메시지 출력
                        Toast.makeText(this, "이미 존재하는 항목입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }



        binding.resetButton.setOnClickListener {
            // 다이얼로그 표시
            showYesNoDialog(
                context = this,
                message = "해당과목의 기록이 모두 삭제됩니다!",
                onYesClicked = {
                    val sub = binding.subTextBtn.text.toString()
                    val myRef2 = uid?.let { it1 ->
                        database.child("users").child(it1)
                    }

                    // ValueEventListener를 통해 데이터베이스의 변경사항을 감지하고 처리합니다.
                    myRef2?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (userSnapshot in dataSnapshot.children) {
                                val existingSub = userSnapshot.child("sub").getValue(String::class.java)
                                if (existingSub == sub) {
                                    // 해당 sub의 시간을 00:00으로 초기화
                                    userSnapshot.ref.child("time").setValue("00:00")
                                    // 해당 과목(sub)을 데이터베이스에서 삭제
                                    userSnapshot.ref.removeValue()
                                    binding.subTextBtn.setText("과목을 변경하세요")
                                    subChange=0
                                    break
                                }
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // 에러 발생 시 처리할 내용을 작성합니다.
                        }
                    })
                },
                onNoClicked = {
                    // '아니오'를 선택했을 때 수행할 작업
                }
            )
        }

        binding.startButton.setOnClickListener {
            if(subChange == 0){
                Toast.makeText(this,"과목을 변경해주세요",Toast.LENGTH_SHORT).show()
            }
            else if (buttonPressCount == 0) {
                startTimer()
                binding.startButton.setText("STOP!")
                buttonPressCount = 1 // 시작 상태로 변경
            } else {
                stopTimer()
                binding.startButton.setText("START!")
                buttonPressCount = 0 // 정지 상태로 변경

                val sub = binding.subTextBtn.text.toString()
                val time = binding.timeView.text.toString()

                val myRef = uid?.let { it1 ->
                    database.child("users").child(it1)
                }

                // ValueEventListener를 통해 데이터베이스의 변경사항을 감지하고 처리합니다.
                if (myRef != null) {
                    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var isSubExist = false
                            for (userSnapshot in dataSnapshot.children) {
                                val existingSub = userSnapshot.child("sub").getValue(String::class.java)
                                if (existingSub == sub) {
                                    // 기존에 동일한 과목이 존재하면 해당 시간을 더합니다.
                                    var existingTime = userSnapshot.child("time").getValue(String::class.java)
                                    val partsExisting = existingTime?.split(":")
                                    val existingMinutes = partsExisting?.get(0)?.toInt()
                                    val existingSeconds = partsExisting?.get(1)?.toInt()

                                    val partsNew = time.split(":")
                                    val newMinutes = partsNew[0].toInt()
                                    val newSeconds = partsNew[1].toInt()

                                    // 시간을 더합니다.
                                    val totalMinutes = existingMinutes?.plus(newMinutes)
                                    val totalSeconds = existingSeconds?.plus(newSeconds)
                                    existingTime = String.format("%02d:%02d", totalMinutes, totalSeconds)

                                    // 데이터베이스에 수정된 시간을 업데이트합니다.
                                    userSnapshot.ref.child("time").setValue(existingTime)
                                    isSubExist = true
                                    break
                                }
                            }

                            // 만약 해당 과목이 존재하지 않으면 새로 추가합니다.
                            if (!isSubExist) {
                                val model = DataModel(sub, time)
                                myRef.push().setValue(model)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // 에러 발생 시 처리할 내용을 작성합니다.
                        }
                    })
                }
            }
        }

        binding.scoreButton.setOnClickListener {

            /*myRef.push().setValue(model)*/
            val intent = Intent(this, ScoreActivity::class.java)
            startActivity(intent)

        }
    }

    /////////////////////////////시계 타이머 구현/////////////////////////////////////////////////////////////
    private fun startTimer() {
        timerTask = timer(period = 9.99.toLong()) {
            time++

            val hour = time / (60 * 100) // 분을 시로 변환
            val minute = (time / 100) % 60 // 분

            runOnUiThread {
                binding.timeView?.text = String.format("%02d:%02d", hour, minute)
            }
        }
    }
    private fun stopTimer() {
        timerTask?.cancel()
    }
    private fun resetTimer(){
        timerTask?.cancel()

        time=0
        binding.timeView?.text="00:00"
    }

    fun showYesNoDialog(context: Context, message: String, onYesClicked: () -> Unit, onNoClicked: () -> Unit) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
            .setCancelable(false)
            .setPositiveButton("예") { dialog, id ->
                // '예' 버튼이 클릭되었을 때 수행할 동작
                resetTimer()
                onYesClicked()
                dialog.dismiss()
            }
            .setNegativeButton("아니오") { dialog, id ->
                // '아니오' 버튼이 클릭되었을 때 수행할 동작
                onNoClicked()
                dialog.dismiss()
            }
        val alert = builder.create()
        alert.show()
    }
}