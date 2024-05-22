package com.dada.puretimer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.dada.puretimer.databinding.ActivityJoinBinding
import com.dada.puretimer.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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
            var isGoToJoin = true

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            } else if (!isValidEmail(email)) {
            Toast.makeText(this, "이메일 형식을 확인해주세요", Toast.LENGTH_LONG).show()
            isGoToJoin = false
            }
            else if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }
            else if(password.length < 6){
                Toast.makeText(this, "비밀번호는 최소 6자 이상이어야 합니다", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }
            else if (passwordChack.isEmpty()) {
                Toast.makeText(this, "비밀번호를 확인해주세요", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }
            else if(password != passwordChack){
                Toast.makeText(this, "비밀번가 일치하지 않습니다.", Toast.LENGTH_LONG).show()
                isGoToJoin = false
            }
            if (isGoToJoin) {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "회원가입 성공!", Toast.LENGTH_LONG).show()
                            val user = auth.currentUser
                            val intent = Intent(this, MainActivity::class.java)
                            intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        } else if (task.exception?.message.isNullOrEmpty()) {
                            Toast.makeText(this, "회원가입에 실패", Toast.LENGTH_LONG).show()
                        } else {
                            //입력한 계정 정보가 이미 Firebase DB에 있는 경우
                        }
                    }
            } else {
            }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        return emailRegex.toRegex().matches(email)
    }

}