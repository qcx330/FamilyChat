package com.example.familychat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.adapter.MessageAdapter
import com.example.familychat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MessageViewModel : ViewModel() {
    private val messageList = MutableLiveData<List<Message>>()

    private val chatRef = FirebaseDatabase.getInstance().getReference("Chat")
    private val auth = FirebaseAuth.getInstance()
    val adapter= MessageAdapter()

    fun getMessageList():LiveData<List<Message>>{
        return messageList
    }
    fun sendMessageUser(content:String, roomChatId:String){
        val message = Message(auth.currentUser!!.uid,content, System.currentTimeMillis())
        chatRef.child("UserChat").child(roomChatId).child("Message").setValue(message)

    }
}