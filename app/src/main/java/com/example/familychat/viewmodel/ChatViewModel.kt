package com.example.familychat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.adapter.ChatAdapter
import com.example.familychat.adapter.RvInterface
import com.example.familychat.model.ChatRoom
import com.example.familychat.model.ChatRoomType
import com.example.familychat.model.Message
import com.example.familychat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatViewModel : ViewModel() {
    private val chatRoomList = MutableLiveData<List<ChatRoom>>()
    private val chatRoomId = MutableLiveData<String?>()
    val otherUser = MutableLiveData<User>()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser!!.uid
    private val userRef = database.getReference("User")
    private val chatRef = database.getReference("Chat")
    val adapter = ChatAdapter(object : RvInterface{
        override fun OnClickItem(pos: Int) {
        }

    })
    fun setChatRoomId(value: String) {
        chatRoomId.value = value
    }
    fun getOtherUser():LiveData<User>{
        return otherUser
    }
    fun getChatRoomList():LiveData<List<ChatRoom>>{
        return chatRoomList
    }
    fun getChatRoomId():LiveData<String?>{
        return chatRoomId
    }
    fun fetchOtherUserDetails(userId: String) {
        val user = fetchDataUserById(userId)
        otherUser.postValue(user)
    }
    fun retrieveFamilyChat(familyId:String){
        chatRef.child("FamilyChat").child(familyId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val roomList = chatRoomList.value.orEmpty().toMutableList()
                val chatRoom = snapshot.getValue(ChatRoom::class.java)
                chatRoom?.let { roomList.add(it) }
                chatRoomList.value = roomList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("family chat", error.message)
            }

        })
    }
    fun fetchDataUserById(userId:String):User{
        var user = User()
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                user = dataSnapshot.getValue(User::class.java)!!
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("get current user", databaseError.message)
            }
        })
        return user
    }
    fun getChatRoom(userId: String){
        chatRef.child("UserChat").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (chatRoomSnapshot in snapshot.children) {
                    val membersSnapshot = chatRoomSnapshot.child("members")
                    val user1Exists = membersSnapshot.hasChild(currentUserId!!)
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
        val currentList = chatRoomList.value.orEmpty().toMutableList()
        val newChatRef = chatRef.child("UserChat").push()
        val members = listOf(currentUserId!!, userId)
        val message = Message(currentUserId, "Hello", System.currentTimeMillis())
        val mapMess = mapOf<String, Message>("WelcomeMessage" to message)
        val room = ChatRoom(newChatRef.key!!,ChatRoomType.USER, "User",message.content, message.time, mapMess, members)
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

    fun retrieveUserChat() {
        chatRef.child("UserChat")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatRooms = chatRoomList.value.orEmpty().toMutableList()
                    for (childSnapshot in snapshot.children) {
                        val chatRoom = childSnapshot.getValue(ChatRoom::class.java)
                        if (chatRoom?.member!!.contains(auth.currentUser!!.uid))
                            chatRooms.add(chatRoom)
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