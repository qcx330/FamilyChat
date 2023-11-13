package com.example.familychat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.adapter.MessageAdapter
import com.example.familychat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MessageViewModel : ViewModel() {
    private val messageList = MutableLiveData<List<Message>>()

    private val chatRef = FirebaseDatabase.getInstance().getReference("Chat")
    private val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    val adapter= MessageAdapter()

    fun getMessageList():LiveData<List<Message>>{
        return messageList
    }
    private fun sendUserMessage(message: String, chatId:String) {
        val messageId = chatRef.child("UserChat").child(chatId).child("message").push().key
        val currentList = messageList.value.orEmpty().toMutableList()
        if (currentUserId != null && messageId != null) {
            val chatMessage = Message(currentUserId, message, System.currentTimeMillis())
            chatRef.child(chatId).child("message")
                .child(messageId).setValue(chatMessage)
                .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    currentList.add(chatMessage)
                    messageList.value = currentList
                    adapter.submitList(currentList)
                } else Log.d("send message to user chat", task.exception.toString())
            }

        }
    }
    private fun sendFamilyMessage(message: String, chatId:String) {
        val messageId = chatRef.child("FamilyChat").child(chatId).child("message").push().key
        val currentList = messageList.value.orEmpty().toMutableList()
        if (currentUserId != null && messageId != null) {
            val chatMessage = Message(currentUserId, message, System.currentTimeMillis())
            chatRef.child(chatId).child("message")
                .child(messageId).setValue(chatMessage)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        currentList.add(chatMessage)
                        messageList.value = currentList
                        adapter.submitList(currentList)
                    } else Log.d("send message to family chat", task.exception.toString())
                }

        }
    }
    private fun retrieveFamilyMessage(chatId:String){
        chatRef.child("FamilyChat").child(chatId)
            .child("message").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(Message::class.java)
                        message?.let { messages.add(it) }
                    }
                    messageList.value = messages
                    adapter.submitList(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("retrieve user message", error.message)
                }

            })
    }
    private fun retrieveUserMessage(chatId:String){
        chatRef.child("UserChat").child(chatId)
            .child("message").addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(Message::class.java)
                        message?.let { messages.add(it) }
                    }
                    messageList.value = messages
                    adapter.submitList(messages)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("retrieve user message", error.message)
                }

            })
    }
}