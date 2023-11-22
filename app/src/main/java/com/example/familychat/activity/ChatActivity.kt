package com.example.familychat.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.Utils
import com.example.familychat.adapter.MessageAdapter
import com.example.familychat.databinding.ActivityChatBinding
import com.example.familychat.model.User
import com.example.familychat.viewmodel.ChatViewModel
import com.example.familychat.viewmodel.MessageViewModel
import com.example.familychat.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messChat: RecyclerView
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var messageViewModel: MessageViewModel
    lateinit var currentChat: String
    lateinit var currentFamily: String
    private val auth = FirebaseAuth.getInstance()
    val adapter = MessageAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)
        val chatId = intent.getStringExtra("id")!!
        currentChat = chatId

        messChat = binding.messageChat
        messChat.layoutManager = LinearLayoutManager(this)
        val layoutManager = messChat.layoutManager as LinearLayoutManager

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
                    binding.tvName.text = "Family"
                    messageViewModel.retrieveFamilyMessage(chatId)
                    messageViewModel.getMessageList().observe(this) { list ->
                        if (list != null) {

                            val sortedList = list.sortedBy { it.time }
                            adapter.submitList(sortedList)
                            adapter.notifyDataSetChanged()
                            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                            if (lastVisibleItemPosition != RecyclerView.NO_POSITION) {
                                messChat.smoothScrollToPosition(lastVisibleItemPosition)
                            } else {
                                messChat.smoothScrollToPosition(adapter.itemCount - 1)
                            }
//                            messChat.post {
//                                val lastItemPosition = adapter.itemCount - 1
//                                messChat.smoothScrollToPosition(lastItemPosition)
//                            }
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
                            Log.d("user", userViewModel.fetchDataUserById(userId).toString())
                            val user = userViewModel.fetchDataUserById(userId)
                            userViewModel.getUser().observe(this) { it ->
                                it?.let { binding.tvName.text = it.name }
                            }

                        } else Log.d("chatroomid", "null")
                    }
                    messageViewModel.retrieveUserMessage(chatId)
                    messageViewModel.getMessageList().observe(this) { list ->
                        if (list != null) {
                            val sortedList = list.sortedBy { it.time }
                            adapter.submitList(sortedList)
                            adapter.notifyDataSetChanged()
                            val lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
                            if (lastVisibleItemPosition != RecyclerView.NO_POSITION) {
                                messChat.smoothScrollToPosition(lastVisibleItemPosition)
                            } else {
                                messChat.smoothScrollToPosition(adapter.itemCount - 1)
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
            val text = binding.edtText.text.toString()
            if (currentFamily == chatId) {
                messageViewModel.sendFamilyMessage(text, chatId)
                messChat.post {
                    val lastItemPosition = adapter.itemCount - 1
                    messChat.smoothScrollToPosition(lastItemPosition)
                }
                userViewModel.getUsersInFamily(chatId)
                userViewModel.getUserList().observe(this) { userList ->
                    if (userList.isNotEmpty()) {
                        sendNotification(text, userList, chatId)
                    }
                }
            } else {
                messageViewModel.sendUserMessage(text, chatId)
                messChat.post {
                    val lastItemPosition = adapter.itemCount - 1
                    messChat.smoothScrollToPosition(lastItemPosition)
                }
                chatViewModel.retrieveMemberList(chatId)
                chatViewModel.getMemberList().observe(this) { userList ->
                    if (userList.isNotEmpty()) {
                        sendNotification(text, userList, chatId)
                    }
                }
            }
            binding.edtText.text.clear()
            adapter.notifyDataSetChanged()

        }
        binding.btnAttach.setOnClickListener() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let {
                if (currentFamily == currentChat) {
                    messageViewModel.sendImageFamilyChat(it, currentFamily)
                    userViewModel.getUserList().observe(this) { userList ->
                        if (userList.isNotEmpty()) {
                            sendNotification("sent a image", userList, currentFamily)
                        }
                    }
                } else {
                    messageViewModel.sendImageUserChat(it, currentChat)
                    chatViewModel.getMemberList().observe(this) { userList ->
                        if (userList.isNotEmpty()) {
                            sendNotification("sent a image", userList, currentChat)
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendNotification(message: String, userList: List<User>, chatId: String) {
        userViewModel.getCurrentUser().observe(this) { currentUser ->
            if (currentUser != null) {
                for (user in userList.filter { it.id!= currentUser.id }) {
                        try {
                            val jsonObject = JSONObject()
                            val notificationObj = JSONObject()
                            if (chatId == currentFamily)
                            {
                                notificationObj.put("title", "Family")
                                notificationObj.put("body", "${currentUser.name}: $message")
                            }
                            else{
                                notificationObj.put("title", currentUser.name)
                                notificationObj.put("body", message)
                            }
                            val dataObj = JSONObject()
                            dataObj.put("chatId", chatId)
                            jsonObject.put("notification", notificationObj)
                            jsonObject.put("data", dataObj)
                            jsonObject.put("to", user.token)
                            Log.d("send notification to", user.name)
                            callApi(jsonObject)
                        } catch (e: Exception) {
                            Log.e("send notification", e.message.toString())
                    }
                }
            }
        }
    }

    fun callApi(jsonObject: JSONObject) {
        val json = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val headers = Headers.headersOf(
            "Authorization",
            "Bearer AAAAk3sOXv0:APA91bF5JwkYRRVKPg-uwuBayOT9MuodeDWzZlnGECxYZs7913bEfe6vwB43FYxALmd5ZUc4udJme4zxB3JeO2juK59QRtNhHWsWCNJsoB3eQbt_4YuVSY8lp1YqDyizFZDwqoHRgKox"
        )

        val body = RequestBody.create(json, jsonObject.toString())

        val request = Request.Builder().apply {
            url(url)
            method("POST", body)
            headers(headers)
        }.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("call back send noti", e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("FCM Response", responseBody ?: "Empty response body")
                } else {
                    Log.e("FCM Request Failed", response.body?.string() ?: "Empty response body")
                }
            }

        })
    }
}