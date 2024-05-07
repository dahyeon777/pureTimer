package com.dada.puretimer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dada.puretimer.databinding.ActivityScoreBinding

class ScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScoreBinding

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_score)

        binding.homeButton.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        val rv_board = findViewById<RecyclerView>(R.id.rv)


        val itemList = ArrayList<BoardItem>()

        itemList.add(BoardItem("수학","13:00"))
        itemList.add(BoardItem("과학","13:00"))
        itemList.add(BoardItem("수학","13:00"))
        itemList.add(BoardItem("과학","13:00"))
        itemList.add(BoardItem("수학","13:00"))
        itemList.add(BoardItem("과학","13:00"))
        itemList.add(BoardItem("수학","13:20"))
        itemList.add(BoardItem("과학","13:00"))
        itemList.add(BoardItem("수학","13:00"))
        itemList.add(BoardItem("과학","13:00"))
        itemList.add(BoardItem("복수전공","13:00"))
        itemList.add(BoardItem("과학","13:00"))

        val boardAdapter = BoardAdapter(itemList)
        rv_board.adapter = boardAdapter
        rv_board.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        boardAdapter.notifyDataSetChanged()

    }
}