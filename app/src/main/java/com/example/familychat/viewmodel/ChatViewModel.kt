package com.example.familychat.viewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.adapter.ChatAdapter
import com.example.familychat.model.ChatRoom
import com.example.familychat.model.Message
import com.example.familychat.view.ChatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatViewModel : ViewModel() {
    private val chatRoomList = MutableLiveData<List<ChatRoom>>()
    private val chatRoomId = MutableLiveData<String?>()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid
    private val chatRef = database.getReference("Chat")
    val adapter = ChatAdapter()
    fun getChatRoomList():LiveData<List<ChatRoom>>{
        return chatRoomList
    }
    fun getChatRoomId():LiveData<String?>{
        return chatRoomId
    }
    fun retrieveFamilyChat(familyId:String){
        chatRef.child("FamilyChat").child(familyId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val roomList = mutableListOf<ChatRoom>()
                val chatRoom = snapshot.getValue(ChatRoom::class.java)
                chatRoom?.let { roomList.add(it) }
                chatRoomList.value = roomList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("family chat", error.message)
            }

        })
    }
    fun getChatRoom(userId: String){
        chatRef.child("UserChat").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chatRoomSnapshot in snapshot.children) {
                    val membersSnapshot = chatRoomSnapshot.child("members")
                    val user1Exists = membersSnapshot.hasChild(auth.currentUser!!.uid)
                    val user2Exists = membersSnapshot.hasChild(userId)

                    if (user1Exists && user2Exists) {
                        chatRoomId.postValue(chatRoomSnapshot.key)
                        Log.d("ChatroomId", chatRoomSnapshot.key.toString())
                        return
                    }
                }
                Log.d("ChatroomId", "Not found")
                createChatRoom(userId)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatroomId", error.message)
                chatRoomId.postValue(null)
            }
        })
    }
    private fun createChatRoom(userId:String){
        val currentList = chatRoomList.value!!.toMutableList()
        val newChatRef = chatRef.child("UserChat").push()
        val members = listOf(auth.currentUser!!.uid, userId)
        val message = Message(auth.currentUser!!.uid, "Hello", System.currentTimeMillis())
        val mapMess = mapOf<String, Message>("WelcomeMessage" to message)
        val room = ChatRoom(newChatRef.key!!, "User",message.content, message.time, mapMess, members)
        newChatRef.setValue(room)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val chatId = newChatRef.key
                    chatRoomId.postValue(chatId)
                    currentList.add(room)
                    chatRoomList.value = currentList
                    adapter.submitList(currentList)
                    println("Chat room created with ID: $chatId")
                } else {
                    println("Failed to create chat room: ${task.exception}")
                }
            }
    }
    private fun sendMessage(message: String, chatId:String) {
        val userId = auth.currentUser?.uid
        val messageId = chatRef.child(chatId).child("message").push().key

        if (userId != null && messageId != null) {
            val chatMessage = Message(userId, message, System.currentTimeMillis())
            chatRef.child(chatId).child("message").child(messageId).setValue(chatMessage)
        }
    }

    fun retrieveUserChat() {
        chatRef.child("UserChat").orderByChild("member/$currentUserId")
            .equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatRooms = mutableListOf<ChatRoom>()
                    for (childSnapshot in snapshot.children) {
                        val chatRoom = childSnapshot.getValue(ChatRoom::class.java)
                        chatRoom?.let { chatRooms.add(it) }
                    }
                    chatRoomList.value = chatRooms
                    adapter.submitList(chatRooms)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("user chat", error.message)
                }
            })
    }
}