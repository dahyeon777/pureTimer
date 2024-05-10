package com.dada.puretimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dada.puretimer.databinding.ActivityJoinBinding
import com.dada.puretimer.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class JoinActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth= Firebase.auth

        binding = DataBindingUtil.setContentView(this, R.layout.activity_join)

        binding.joinOkBtn.setOnClickListener {

            val email = binding.joinEmailArea.text.toString()
            val password = binding.passwordEmailArea.text.toString()
            val passwordChack = binding.passwordCheckArea.text.toString()

            if (password == passwordChack) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "회원가입에 성공했습니다.", Toast.LENGTH_LONG).show()
                        } else if (task.exception?.message.isNullOrEmpty()) {
                            Toast.makeText(this, "회원가입에 실패했습니다.", Toast.LENGTH_LONG).show()
                        } else {
                            //입력한 계정 정보가 이미 Firebase DB에 있는 경우
                            Toast.makeText(this, "계정이 이미 존재합니다.", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_LONG).show()
            }
        }
    }
}