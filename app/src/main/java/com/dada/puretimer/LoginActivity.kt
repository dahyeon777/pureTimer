package com.dada.puretimer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dada.puretimer.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        auth= Firebase.auth

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        /*binding.loginOkBtn.setOnClickListener {

            val email = binding.emailArea.text.toString()
            val password=binding.passwordArea.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this,"회원가입에 성공했습니다.",Toast.LENGTH_LONG).show()
                    } else if (task.exception?.message.isNullOrEmpty()) {
                        Toast.makeText(this, "회원가입에 실패했습니다.", Toast.LENGTH_LONG).show()
                    }else {
                        //입력한 계정 정보가 이미 Firebase DB에 있는 경우
                        Toast.makeText(this, "계정이 이미 존재합니다.", Toast.LENGTH_LONG).show()
                    }
                }
        }*/
        binding.loginOkBtn.setOnClickListener {

            val email = binding.emailArea.text.toString()
            val password=binding.passwordArea.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    signIn(email, password)
                }
        }
    }



    //회원가입 및 로그인 성공 시 메인 화면으로 이동하는 함수
    fun goToMainActivity(user: FirebaseUser?) {
        //Firebase에 등록된 계정일 경우에만 메인 화면으로 이동
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    //로그인 함수
    fun signIn(email: String, password: String) {
        auth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //로그인에 성공한 경우 메인 화면으로 이동
                    goToMainActivity(task.result?.user)
                } else {
                    //로그인에 실패한 경우 Toast 메시지로 에러를 띄워준다
                    Toast.makeText(this, "아이디,비밀번호가 다릅니다.", Toast.LENGTH_LONG).show()
                }
            }
    }
}