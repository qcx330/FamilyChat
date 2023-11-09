package com.example.familychat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.model.ChatRoom
import com.example.familychat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatViewModel : ViewModel() {
    private val chatRoomList = MutableLiveData<List<ChatRoom>>()
    private val chatRoom = MutableLiveData<String?>()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser?.uid
    private val chatRef = database.getReference("Chat")

    fun getChatRoomList():LiveData<List<ChatRoom>>{
        return chatRoomList
    }
    fun getChatRoom():LiveData<String?>{
        return chatRoom
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
    fun checkChatRoom(user1Id: String, user2Id: String) {
        chatRef.orderByChild("members/$user1Id").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (childSnapshot in snapshot.children) {
                        val room = childSnapshot.getValue(ChatRoom::class.java)
                        if (room != null && room.members?.contains(user2Id) == true) {
                            chatRoom.value = room.roomId
                        }
                        else {
                            createChatforUser(user1Id, user2Id)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("Chat room exists", error.message)
                }
            })
    }
    private fun sendMessage(message: String, chatId:String) {
        val userId = auth.currentUser?.uid
        val messageId = chatRef.child(chatId).child("message").push().key

        if (userId != null && messageId != null) {
            val chatMessage = Message(userId, message, System.currentTimeMillis())
            chatRef.child(chatId).child("message").child(messageId).setValue(chatMessage)
        }
    }
    fun createChatforUser(user1Id:String, user2Id:String){
        val chatId = chatRef.child("UserChat").push().key
        val member = listOf(user1Id, user2Id)
        val chat = ChatRoom(chatId!!,"", "", 0, null, member)
        chatRef.child("UserChat").child(chatId).setValue(chat).addOnCompleteListener {
            if (it.isSuccessful){
                chatRoom.value = chatId
                Log.d("Create user chat", "Successfully")
            }
            else Log.d("Create user chat", "Fail")
        }
    }
    fun retrieveUserChat() {
        chatRef.child("UserChat").orderByChild("members/$currentUserId")
            .equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatRooms = mutableListOf<ChatRoom>()
                    for (childSnapshot in snapshot.children) {
                        val chatRoom = childSnapshot.getValue(ChatRoom::class.java)
                        chatRoom?.let { chatRooms.add(it) }
                    }
                    chatRoomList.value = chatRooms
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("user chat", error.message)
                }
            })
    }
}