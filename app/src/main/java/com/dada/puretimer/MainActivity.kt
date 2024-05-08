package com.dada.puretimer

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.dada.puretimer.databinding.ActivityMainBinding
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var time = 0
    private var timerTask: Timer? = null
    private var buttonPressCount = 0


    private lateinit var subArray: MutableList<String>
    private lateinit var sharedPref: SharedPreferences

    private fun enablePersistence() {
        // 오프라인에서도 데이터저장 가능하게 함
        Firebase.database.setPersistenceEnabled(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)


        sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)

        // 저장된 데이터를 로드합니다.
        val savedColors =
            sharedPref.getStringSet("colorArray", HashSet<String>())?.toList() ?: emptyList()
        subArray = savedColors.toMutableList()


        binding.changeButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val input = EditText(this) // EditText를 생성

            builder.setTitle("과목을 선택하세요.")
                .setItems(subArray.toTypedArray()) { dialog, which ->
                    // 여기서 인자 'which'는 배열의 position을 나타냅니다.
                    binding.subText.text = subArray[which]
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

            /*Toast.makeText(this, "해당과목의 기록이 초기화됩니다", Toast.LENGTH_SHORT).show()*/
            resetTimer()


        }
        binding.startButton.setOnClickListener {
            if (buttonPressCount == 0) {
                startTimer()
                binding.startButton.setText("STOP!")
                buttonPressCount = 1 // 시작 상태로 변경
            } else {
                stopTimer()
                binding.startButton.setText("START!")
                buttonPressCount = 0 // 정지 상태로 변경
            }
        }
        binding.scoreButton.setOnClickListener {

            val sub_list=binding.subText.text.toString()
            val time_list=binding.timeView.text.toString()

            val database = Firebase.database
            val myRef = database.getReference("timeList")

            val model=DataModel(sub_list,time_list)
            myRef.push().setValue(model)

            val intent = Intent(this, ScoreActivity::class.java)
            startActivity(intent)
        }
    }

    /////////////////////////////시계 타이머 구현/////////////////////////////////////////////////////////////
    private fun startTimer() {
        timerTask = timer(period = 10) {
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
    ///////////////////////////////////////////////////////////////////////////////////////////////////
}