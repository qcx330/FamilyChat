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
import com.google.firebase.database.childEvents

class ChatViewModel : ViewModel() {
    private val chatRoomList = MutableLiveData<List<ChatRoom>>()
    private val chatRoomId = MutableLiveData<String?>()
    private val chatRoom = MutableLiveData<ChatRoom?>()
    private val memberList = MutableLiveData<List<User>>()

    private val database = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val currentUserId = auth.currentUser!!.uid
    private val userRef = database.getReference("User")
    private val chatRef = database.getReference("Chat")
    private val familyRef = database.getReference("Family")
    val adapter = ChatAdapter(object : RvInterface{
        override fun OnClickItem(pos: Int) {
        }

    })
    fun getMemberList():LiveData<List<User>>{
        return memberList
    }
    fun getChatRoom():LiveData<ChatRoom?>{
        return chatRoom
    }
    fun getChatRoomList():LiveData<List<ChatRoom>>{
        return chatRoomList
    }
    fun getChatRoomId():LiveData<String?>{
        return chatRoomId
    }
    fun retrieveMemberList(chatId:String){
        val users = mutableListOf<User>()
        chatRef.child("UserChat").child(chatId).child("member")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (memberSnapshot in snapshot.children) {
                        val user = memberSnapshot.getValue(User::class.java)
                        user?.let { users.add(it) }
                    }
                    memberList.value = users
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
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
    fun getFamilyChat(familyId: String){
        chatRef.child("FamilyChat")
            .child(familyId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val room = snapshot.getValue(ChatRoom::class.java)
                    if (room != null) {
                        chatRoom.postValue(room)
                    } else {
                        Log.e("getFamilyChat", "null")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("getFamilyChat", "Error: ${error.message}")
                }
            })
    }
    fun getChatRoom(chatRoomId:String){
        chatRef.child("UserChat")
            .child(chatRoomId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val room = snapshot.getValue(ChatRoom::class.java)
                if (room != null) {
                    chatRoom.postValue(room)
                } else {
                    Log.e("getChatRoomData", "null")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("getChatRoomData", "Error: ${error.message}")
            }
        })
    }
    fun getChatRoom(currentUser:User, user: User){
        chatRef.child("UserChat").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children)
                {
                    val membersSnapshot = childSnapshot.child("member")
                    val users = mutableListOf<User>()

                    for (memberSnapshot in membersSnapshot.children) {
                        val member = memberSnapshot.getValue(User::class.java)
                        member?.let { users.add(it) }
                    }
                    if (users.contains(currentUser) && users.contains(user)) {
                        chatRoomId.postValue(childSnapshot.key)
                        Log.d("ChatroomId", childSnapshot.key.toString())
                        chatRoomId.postValue(childSnapshot.key.toString())
                        return
                    }
                }
                Log.d("ChatroomId", "Not found")
                createChatRoom(currentUser, user)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ChatroomId", error.message)
                chatRoomId.postValue(null)
            }
        })
    }
    private fun createChatRoom(currentUser :User,user: User){
        val currentList = chatRoomList.value.orEmpty().toMutableList()
        val newChatRef = chatRef.child("UserChat").push()
        val members = listOf(currentUser, user)
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
                        val membersSnapshot = childSnapshot.child("member")
                        val membersList = mutableListOf<User>()

                        for (memberSnapshot in membersSnapshot.children) {
                            val user = memberSnapshot.getValue(User::class.java)
                            user?.let { membersList.add(it) }
                        }

                        if (membersList.any { it.id == currentUserId }) {
                            val chatRoom = childSnapshot.getValue(ChatRoom::class.java)
                            chatRoom?.let { chatRooms.add(it) }
                        }
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