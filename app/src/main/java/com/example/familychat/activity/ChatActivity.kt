package com.example.familychat.activity

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.adapter.MessageAdapter
import com.example.familychat.databinding.ActivityChatBinding
import com.example.familychat.viewmodel.ChatViewModel

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messChat : RecyclerView
    private lateinit var chatViewModel: ChatViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)


        val adapter = MessageAdapter()
        messChat = binding.messageChat
        messChat.layoutManager = LinearLayoutManager(this)
        messChat.adapter = adapter

        binding.btnBack.setOnClickListener(){
            finish()
        }
        binding.btnSend.setOnClickListener(){
            chatViewModel.getChatRoomId().observe(this){
                it ->if (it != null)
                    Log.d("get chat room id", it)
                else Log.e("get chat room id", "null")
            }
        }
    }
}