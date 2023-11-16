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
import com.example.familychat.model.MessageType
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
        chatRef.child("UserChat").child(chatId).child("member")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userIds = mutableListOf<String>()

                    for (userIdSnapshot in snapshot.children) {
                        val userId = userIdSnapshot.value
                        userId?.let { userIds.add(it.toString()) }
                    }
                    fetchUserDetails(userIds)

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("retrieveMemberList", error.message)
                }
            })
    }

    fun fetchUserDetails(userIds: List<String>) {
        val users = mutableListOf<User>()
        for (userId in userIds) {
            val userId= userRef.child(userId)
            userId.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(userSnapshot: DataSnapshot) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null) {
                        users.add(user)
                    }
                    memberList.postValue(users)
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("fetchUserDetails", error.message)
                }
            })
        }
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
    fun getChatRoomWithUser(userId: String){
        chatRef.child("UserChat").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (childSnapshot in snapshot.children)
                {
//                    val membersSnapshot = childSnapshot.child("member")
//                    val users = mutableListOf<User>()
//
//                    val user1Exists = membersSnapshot.hasChild(currentUserId)
//                    val user2Exists = membersSnapshot.hasChild(userId)
//                    if (user1Exists && user2Exists) {
//                        chatRoomId.postValue(childSnapshot.key)
//                        return
//                    }
                    val chatId = childSnapshot.key
                    if (chatId != null && chatId.contains(currentUserId) && chatId.contains(userId)) {
                        chatRoomId.postValue(childSnapshot.key)
                        Log.d("YourViewModel", "Chat room found: $chatRoomId")
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
    private fun createChatRoom(userId: String){
        val currentList = chatRoomList.value.orEmpty().toMutableList()
//        val newChatRef = chatRef.child("UserChat").push()
        val chatId = "$currentUserId-$userId"
        val newChatRef = chatRef.child("UserChat")
        val members = listOf(currentUserId, userId)
        val message = Message(currentUserId, "Hello", System.currentTimeMillis(), MessageType.TEXT)
        val mapMess = mapOf<String, Message>("WelcomeMessage" to message)
        val room = ChatRoom(chatId,ChatRoomType.USER, "User",message.content, message.time, mapMess, members)
        newChatRef.child(chatId).setValue(room)
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
        chatRef.child("UserChat").orderByKey().startAt("$currentUserId-")
            .endAt("$currentUserId-\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val chatRooms = chatRoomList.value.orEmpty().toMutableList()
                    for (childSnapshot in snapshot.children) {
                        val chatId = childSnapshot.key
                        if (chatId != null && chatId.contains(currentUserId)) {
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