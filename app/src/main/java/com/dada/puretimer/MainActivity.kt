package com.dada.puretimer

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.dada.puretimer.databinding.ActivityMainBinding
import com.google.firebase.auth.EmailAuthProvider
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
    private var mAuth: FirebaseAuth? = null


    private lateinit var subArray: MutableList<String>
    private lateinit var sharedPref: SharedPreferences

    // 오프라인에서도 데이터저장 가능하게 함
    private fun enablePersistence() {
        Firebase.database.setPersistenceEnabled(true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!isNetworkAvailable(this)) {
            showAlertDialog()
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        auth = Firebase.auth
        database = Firebase.database.reference


        val user = FirebaseAuth.getInstance().currentUser // 로그인한 유저의 정보 가져오기
        val uid = user?.uid // 로그인한 유저의 고유 uid 가져오기
        /*Toast.makeText(this,uid,Toast.LENGTH_SHORT).show()*/
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
                    subChange = 1

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

                message = "해당과목의 기록이 모두 삭제됩니다.\n삭제하시겠습니까?",
                onYesClicked = {
                    if (subChange == 0) {
                        Toast.makeText(this, "초기화 할 과목을 선택하세요", Toast.LENGTH_SHORT).show()
                    }
                    val sub = binding.subTextBtn.text.toString()
                    val myRef2 = uid?.let { it1 ->
                        database.child("users").child(it1)
                    }

                    // ValueEventListener를 통해 데이터베이스의 변경사항을 감지하고 처리합니다.
                    myRef2?.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            for (userSnapshot in dataSnapshot.children) {
                                val existingSub =
                                    userSnapshot.child("sub").getValue(String::class.java)

                                if (existingSub == sub) {
                                    // 해당 sub의 시간을 00:00으로 초기화
                                    userSnapshot.ref.child("time").setValue("00:00:00")
                                    // 해당 과목(sub)을 데이터베이스에서 삭제
                                    userSnapshot.ref.removeValue()
                                    binding.subTextBtn.setText("과목을 변경하세요")
                                    subChange = 0
                                    time = 0
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
            if (subChange == 0) {
                Toast.makeText(this, "과목을 변경해주세요", Toast.LENGTH_SHORT).show()
            } else if (buttonPressCount == 0) {
                startTimer()
                binding.startButton.setText("STOP")
                buttonPressCount = 1 // 시작 상태로 변경
            } else {
                stopTimer()
                binding.startButton.setText("START")
                buttonPressCount = 0 // 정지 상태로 변경

                val sub = binding.subTextBtn.text.toString()
                val time = binding.timeView.text.toString()
                binding.timeView.setText("00:00:00")
                this.time = 0
                val toast = Toast.makeText(this, time + "추가 완료!", Toast.LENGTH_LONG).show()


                val myRef = uid?.let { it1 ->
                    database.child("users").child(it1)
                }

                if (myRef != null) {
                    myRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            var isSubExist = false
                            for (userSnapshot in dataSnapshot.children) {
                                val existingSub =
                                    userSnapshot.child("sub").getValue(String::class.java)
                                if (existingSub == sub) {
                                    // 기존에 동일한 과목이 존재하면 해당 시간을 더합니다.
                                    var existingTime =
                                        userSnapshot.child("timeTotal").getValue(String::class.java)
                                    val partsExisting = existingTime?.split(":")
                                    val existingHours = partsExisting?.get(0)?.toInt()
                                    val existingMinutes = partsExisting?.get(1)?.toInt()
                                    val existingSeconds = partsExisting?.get(2)?.toInt()

                                    val partsNew = time.split(":")
                                    val newHours = partsNew[0].toInt()
                                    val newMinutes = partsNew[1].toInt()
                                    val newSeconds = partsNew[2].toInt()

                                    // 시간을 더합니다.
                                    val totalHours = existingHours?.plus(newHours)
                                    val totalMinutes = existingMinutes?.plus(newMinutes)
                                    val totalSeconds = existingSeconds?.plus(newSeconds)
                                    existingTime = String.format(
                                        "%02d:%02d:%02d",
                                        totalHours,
                                        totalMinutes,
                                        totalSeconds
                                    )

                                    // 데이터베이스에 수정된 시간을 업데이트합니다.
                                    userSnapshot.ref.child("timeTotal").setValue(existingTime)
                                    userSnapshot.ref.child("time").setValue("00:00:00")
                                    isSubExist = true
                                    break
                                }
                            }

                            // 만약 해당 과목이 존재하지 않으면 새로 추가합니다.
                            if (!isSubExist) {
                                val model = DataModel(sub, time, timeTotal = time)
                                myRef.push().setValue(model)
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // 에러 발생 시 처리할 내용을 작성합니다.
                        }
                    })
                }
                this.time = 0
                binding.timeView.setText("00:00:00")
            }
        }



        binding.scoreButton.setOnClickListener {
            /*myRef.push().setValue(model)*/
            val intent = Intent(this, ScoreActivity::class.java)
            startActivity(intent)

        }

        binding.logoutBtn.setOnClickListener {
            showLogoutDialog()

        }
    }


    private fun startTimer() {
        time = 0
        timerTask = timer(period = 9.99.toLong()) {
            time++

            val hour = time / (60 * 60 * 100) // 시
            val minute = (time / (60 * 100)) % 60 // 분을 구하고 시간 단위를 제외한 나머지 분
            val second = (time / 100) % 60

            runOnUiThread {
                binding.timeView?.text = String.format("%02d:%02d:%02d", hour, minute, second)
            }
        }
    }

    private fun stopTimer() {
        timerTask?.cancel() // 타이머를 취소합니다.

    }

    private fun resetTimer() {
        timerTask?.cancel()

        time = 0
        binding.timeView?.text = "00:00:00"
    }

    fun showYesNoDialog(
        context: Context,
        message: String,
        onYesClicked: () -> Unit,
        onNoClicked: () -> Unit
    ) {
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

    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)

        builder.setMessage("로그아웃 하시겠습니까?\n(비회원은 데이터가 모두 사라집니다.)")

        builder.setPositiveButton("예") { dialogInterface: DialogInterface, i: Int ->
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null && currentUser.providerData.any { it.providerId == EmailAuthProvider.PROVIDER_ID }) {
                // 이메일 회원가입 유저일 경우 로그아웃만 수행
                Firebase.auth.signOut()
            } else {
                // 비회원일 경우 회원 탈퇴 처리
                mAuth = FirebaseAuth.getInstance()
                val userId = mAuth!!.currentUser?.uid
                if (userId != null) {
                    // 해당 유저의 데이터를 Realtime Database에서 삭제
                    val database = FirebaseDatabase.getInstance()
                    val userRef = database.getReference("users").child(userId)
                    userRef.removeValue()

                    // 여기서 Firestore 등 다른 데이터베이스에서도 해당 유저의 데이터를 삭제할 수 있습니다.
                }
                mAuth!!.currentUser?.delete()
                Firebase.auth.signOut()
            }
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
        }

        builder.setNegativeButton("아니요") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }

        builder.show()
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


    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val nw = connectivityManager.activeNetwork ?: return false
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

}
