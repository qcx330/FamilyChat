package com.example.familychat.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.adapter.MessageAdapter
import com.example.familychat.databinding.ActivityChatBinding
import com.example.familychat.model.User
import com.example.familychat.viewmodel.ChatViewModel
import com.example.familychat.viewmodel.MessageViewModel
import com.example.familychat.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messChat: RecyclerView
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var messageViewModel: MessageViewModel
    lateinit var currentChat :String
    lateinit var currentFamily :String
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        val chatId = intent.getStringExtra("id")!!
        currentChat = chatId
        val adapter = MessageAdapter()
        messChat = binding.messageChat
        messChat.layoutManager = LinearLayoutManager(this)
        messChat.adapter = adapter

        userViewModel.getCurrentFamily()
        userViewModel.getCurrentFamilyId().observe(this) { familyId ->
            if (familyId != null) {
                currentFamily = familyId
                if (familyId == chatId) {
                    chatViewModel.retrieveFamilyChat(familyId)
                    chatViewModel.getChatRoom().observe(this) {
                        if (it != null) {
                            binding.tvName.text = it.roomName
                            Log.d("chatroomName", it.roomName!!)
                        }
                    }
//                    binding.tvName.text = "Family"
                    messageViewModel.retrieveFamilyMessage(chatId)
                    messageViewModel.getMessageList().observe(this) { list ->
                        if (list != null) {
                            userViewModel.getUsersInFamily(familyId)
                            userViewModel.getUserList().observe(this) { members ->
                                if (members.isNotEmpty() && list.isNotEmpty()) {
                                    Log.d("members", members.toString())
                                    adapter.submitList(list)
                                    adapter.submitUser(members)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        } else Log.d("Chat list", "null")
                    }
                } else {
                    chatViewModel.getChatRoom(chatId)
                    chatViewModel.getChatRoom().observe(this) {
                        if (it != null) {
                            val userId =
                                it.member!!.firstOrNull { it != auth.currentUser!!.uid }
                            Log.d("chatroomid", it.roomId!!)
                            Log.d("userId", userId!!)
                            Log.d("user",userViewModel.fetchDataUserById(userId).toString() )
                            val user = userViewModel.fetchDataUserById(userId)
                            userViewModel.getUser().observe(this){
                                it -> it?.let { binding.tvName.text = it.name }
                            }

                        }
                        else Log.d("chatroomid", "null")
                    }
                    messageViewModel.retrieveUserMessage(chatId)
                    messageViewModel.getMessageList().observe(this) { list ->
                        if (list != null) {
                            adapter.submitList(list)
                            chatViewModel.retrieveMemberList(chatId)
                            chatViewModel.getMemberList().observe(this) { members ->
                                adapter.submitUser(members)
                                adapter.notifyDataSetChanged()
                            }
                        } else Log.d("Chat list", "null")
                    }
                }

            } else Log.d("getFamilyIdChat", "null")
        }

        binding.btnBack.setOnClickListener() {
            finish()
        }
        binding.btnSend.setOnClickListener() {
            if (currentFamily == chatId)
                messageViewModel.sendFamilyMessage(binding.edtText.text.toString(), chatId)
            else messageViewModel.sendUserMessage(binding.edtText.text.toString(), chatId)
            binding.edtText.text.clear()
            adapter.notifyDataSetChanged()
        }
        binding.btnAttach.setOnClickListener(){
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let{
                if (currentFamily == currentChat)
                    messageViewModel.sendImageFamilyChat(it, currentFamily)
                else messageViewModel.sendImageUserChat(it, currentChat)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}