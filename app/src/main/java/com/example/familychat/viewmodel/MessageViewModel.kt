package com.example.familychat.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.adapter.MessageAdapter
import com.example.familychat.model.Message
import com.example.familychat.model.MessageType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class MessageViewModel : ViewModel() {
    private val messageList = MutableLiveData<List<Message>>()

    private val chatRef = FirebaseDatabase.getInstance().getReference("Chat")
    private val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private val storageReference = FirebaseStorage.getInstance().reference
//    val adapter= MessageAdapter()


    fun getMessageList():LiveData<List<Message>>{
        return messageList
    }
    fun sendUserMessage(message: String, chatId:String) {
        val chatRoomRef = chatRef.child("UserChat").child(chatId)
        val messageId = chatRoomRef.child("message").push().key
        val currentList = messageList.value.orEmpty().toMutableList()
        if (messageId != null) {
            val chatMessage = Message(messageId,currentUserId, message, System.currentTimeMillis(), MessageType.TEXT)
            chatRef.child("UserChat").child(chatId).child("message")
                .child(messageId).setValue(chatMessage)
                .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    chatRoomRef.child("lastMessage").setValue(message)
                    chatRoomRef.child("timestamp").setValue(System.currentTimeMillis())
                    currentList.add(chatMessage)
                    messageList.value = currentList
                } else Log.d("send message to user chat", task.exception.toString())
            }

        }
    }
    fun sendFamilyMessage(message: String, chatId:String) {
        val familyChatRef = chatRef.child("FamilyChat").child(chatId)
        val messageId = familyChatRef.child("message").push().key
        val currentList = messageList.value.orEmpty().toMutableList()
        if (messageId != null) {
            val chatMessage = Message(messageId,currentUserId, message, System.currentTimeMillis(), MessageType.TEXT)
            chatRef.child("FamilyChat").child(chatId).child("message")
                .child(messageId).setValue(chatMessage)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        familyChatRef.child("lastMessage").setValue(message)
                        familyChatRef.child("timestamp").setValue(System.currentTimeMillis())
                        currentList.add(chatMessage)
                        messageList.value = currentList
                    } else Log.d("send message to family chat", task.exception.toString())
                }

        }
    }
    fun retrieveFamilyMessage(chatId:String){
        chatRef.child("FamilyChat").child(chatId)
            .child("message").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(Message::class.java)
                        message?.let { messages.add(it) }
                    }
                    messageList.value = messages
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("retrieve user message", error.message)
                }

            })
    }
    fun retrieveUserMessage(chatId:String){
        chatRef.child("UserChat").child(chatId)
            .child("message").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = mutableListOf<Message>()
                    for (messageSnapshot in snapshot.children) {
                        val message = messageSnapshot.getValue(Message::class.java)
                        message?.let { messages.add(it) }
                    }
                    messageList.value = messages
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("retrieve user message", error.message)
                }

            })
    }
    fun sendImageUserChat(imageUri: Uri, chatId:String){
        val imageName = "${System.currentTimeMillis()}.jpg"
        val imageRef = storageReference.child("images/$imageName")
        imageRef.putFile(imageUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageRef.downloadUrl.addOnCompleteListener { downloadUrlTask ->
                    if (downloadUrlTask.isSuccessful) {
                        val downloadUrl = downloadUrlTask.result.toString()
                        saveImageDownloadUrlToDatabase(downloadUrl, chatId)
                        Log.d("DownloadUrl", downloadUrl)
                    } else {
                        Log.d("Get download url", "Error getting download URL")
                    }
                }
            } else {
                Log.d("Upload image","error uploading the image")
            }
        }
    }
    fun sendImageFamilyChat(imageUri: Uri, chatId:String){
        val currentList = messageList.value.orEmpty().toMutableList()
        val imageName = "${System.currentTimeMillis()}.jpg"
        val imageRef = storageReference.child("images/$imageName")
        imageRef.putFile(imageUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageRef.downloadUrl.addOnCompleteListener { downloadUrlTask ->
                    if (downloadUrlTask.isSuccessful) {
                        val downloadUrl = downloadUrlTask.result.toString()
                        val chatRoomRef = chatRef.child("FamilyChat").child(chatId)
                        val messageId = chatRoomRef.child("message").push().key
                        if (messageId != null) {
                            val message = Message(messageId,currentUserId, downloadUrl, System.currentTimeMillis(), MessageType.IMAGE)
                            chatRoomRef.child("message")
                                .child(messageId).setValue(message)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        chatRoomRef.child("lastMessage").setValue("sent a image")
                                        chatRoomRef.child("timestamp").setValue(System.currentTimeMillis())
                                        currentList.add(message)
                                        messageList.value = currentList
//                                        adapter.submitList(currentList)
                                    } else Log.d("send message to family chat", task.exception.toString())
                                }
                        }
                        Log.d("DownloadUrl", downloadUrl)
                    } else {
                        Log.d("Get download url", "Error getting download URL")
                    }
                }
            } else {
                Log.d("Upload image","error uploading the image")
            }
        }
    }
    private fun saveImageDownloadUrlToDatabase(downloadUrl: String, chatId: String) {
        val chatRoomRef = chatRef.child("UserChat").child(chatId)
        val messageId = chatRoomRef.child("message").push().key
        val currentList = messageList.value.orEmpty().toMutableList()
        if (messageId != null) {
            val message = Message(messageId,currentUserId, downloadUrl, System.currentTimeMillis(), MessageType.IMAGE)
            chatRef.child("UserChat").child(chatId).child("message")
                .child(messageId).setValue(message)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        chatRoomRef.child("lastMessage").setValue("sent a image")
                        chatRoomRef.child("timestamp").setValue(System.currentTimeMillis())
                        currentList.add(message)
                        messageList.value = currentList
//                        adapter.submitList(currentList)
                    } else Log.d("send message to user chat", task.exception.toString())
                }

        }
    }
    fun sendLocation(location:String, chatId:String, typeChat:String){
        val chatRoomRef = chatRef.child(typeChat).child(chatId)
        val messageId = chatRoomRef.child("message").push().key
        val currentList = messageList.value.orEmpty().toMutableList()
        if (messageId != null) {
            val chatMessage = Message(messageId,currentUserId, location, System.currentTimeMillis(), MessageType.LOCATION)
            chatRef.child(typeChat).child(chatId).child("message")
                .child(messageId).setValue(chatMessage)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        chatRoomRef.child("lastMessage").setValue("sent a location")
                        chatRoomRef.child("timestamp").setValue(System.currentTimeMillis())
                        currentList.add(chatMessage)
                        messageList.value = currentList
                    } else Log.d("send location to user chat", task.exception.toString())
                }

        }
    }
}