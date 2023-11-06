package com.example.familychat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.model.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatViewModel : ViewModel() {
    private val chatRoomList = MutableLiveData<List<ChatRoom>>()
    val chatRoomListLiveData: LiveData<List<ChatRoom>> = chatRoomList

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid
    private fun retrieveChatList() {
        val chatRef = database.getReference("Chat")
        chatRef.orderByChild("memberIds/$currentUserId").equalTo(true).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val chatRoomRooms = mutableListOf<ChatRoom>()
                for (chatSnapshot in dataSnapshot.children) {
                    val chatRoom = chatSnapshot.getValue(ChatRoom::class.java)
                    chatRoom?.let {
                        chatRoomRooms.add(it)
                    }
                }
                chatRoomList.value = chatRoomRooms
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle the error
            }
        })
    }

}