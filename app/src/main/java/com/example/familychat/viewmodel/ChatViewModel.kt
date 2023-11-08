package com.example.familychat.viewmodel

import android.util.Log
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

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid
    private val chatRef = database.getReference("Chat")

    fun getChatRoomList():LiveData<List<ChatRoom>>{
        return chatRoomList
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

    fun retrieveChatList() {
        chatRef.child("UserChat").orderByChild("memberIds/$currentUserId")
            .equalTo(true)
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val roomList = mutableListOf<ChatRoom>()
                for (chatSnapshot in dataSnapshot.children) {
                    val chatRoom = chatSnapshot.getValue(ChatRoom::class.java)
                    chatRoom?.let {
                        roomList.add(it)
                    }
                }
                chatRoomList.value = roomList
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("chat list", databaseError.message)
            }
        })
    }
}