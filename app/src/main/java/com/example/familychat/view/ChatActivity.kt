package com.example.familychat.view

import android.content.Intent
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.R
import com.example.familychat.adapter.MessageAdapter
import com.example.familychat.databinding.ActivityChatBinding

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messChat : RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = Intent()
        val userId = intent.getStringExtra("id")

        val adapter = MessageAdapter()
        messChat = binding.messageChat
        messChat.layoutManager = LinearLayoutManager(this)
        messChat.adapter = adapter

        binding.btnBack.setOnClickListener(){
            finish()
        }
        binding.btnSend.setOnClickListener(){

        }
    }
}