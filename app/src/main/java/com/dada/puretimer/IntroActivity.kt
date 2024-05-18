package com.dada.puretimer

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.dada.puretimer.databinding.ActivityIntroBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(!isNetworkAvailable(this)){
            showAlertDialog()
        }

        auth = Firebase.auth
        binding = DataBindingUtil.setContentView(this, R.layout.activity_intro)

        if (auth.currentUser != null) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        binding.joinBtn.setOnClickListener {
            val intent = Intent(this,JoinActivity::class.java)
            startActivity(intent)
        }

        binding.anonyBtn.setOnClickListener {
            auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(this, "로그인에 성공!", Toast.LENGTH_LONG).show()
                        val user = auth.currentUser
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(this, "로그인에 실패", Toast.LENGTH_LONG).show()
                    }
                }
        }
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
            .setMessage("인터넷 연결을 확인해주세요.\n앱이 종료됩니다.")
            .setPositiveButton("확인") { dialog, which ->
                // 앱 종료
                finish()
            }
            .setCancelable(false)
            .show()
    }
}