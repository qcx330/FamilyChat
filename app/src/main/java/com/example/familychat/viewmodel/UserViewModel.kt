package com.example.familychat.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.adapter.RvInterface
import com.example.familychat.adapter.UserAdapter
import com.example.familychat.model.ChatRoom
import com.example.familychat.model.ChatRoomType
import com.example.familychat.model.Message
import com.example.familychat.model.MessageType
import com.example.familychat.model.User
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlin.random.Random

class UserViewModel (): ViewModel() {
    private val userList = MutableLiveData<List<User>>()
    val currentFamilyId = MutableLiveData<String?>()
    val user = MutableLiveData<User>()
    val statusAdd = MutableLiveData<Boolean>()
    val adapter = UserAdapter(object :RvInterface{
        override fun OnClickItem(pos: Int) {
        }

        override fun OnRemoveItem(pos: Int) {
        }
    })

    val currentUser = MutableLiveData<User>()

    //Firebase
    private val storageReference = FirebaseStorage.getInstance().reference
    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val familyRef = FirebaseDatabase.getInstance().getReference("Family")
    private val chatRef = FirebaseDatabase.getInstance().getReference("Chat")
    private val auth = FirebaseAuth.getInstance()

    init{
        userList.value = mutableListOf()
        loadCurrentUser()
    }
    fun getStatusAdd():LiveData<Boolean>{
        return statusAdd
    }
    fun getCurrentUser():LiveData<User>{
        return currentUser
    }
    fun getUser():LiveData<User>{
        return user
    }
    fun getUserList(): LiveData<List<User>> {
        return userList
    }
    fun getCurrentFamilyId():LiveData<String?>{
        return currentFamilyId
    }
    private fun loadCurrentUser() {
        val current = auth.currentUser
        if (current != null) {
            val userId = current.uid

            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let { currentUser.value = it }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d("get current user", databaseError.message)
                }
            })
        }
    }
    fun setUserAvatar(imageUri:Uri){
        val imageName = "${System.currentTimeMillis()}.jpg"
        val imageRef = storageReference.child("images/$imageName")
        imageRef.putFile(imageUri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                imageRef.downloadUrl.addOnCompleteListener { downloadUrlTask ->
                    if (downloadUrlTask.isSuccessful) {
                        val downloadUrl = downloadUrlTask.result.toString()
                        saveImageDownloadUrlToDatabase(downloadUrl)
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
    fun saveImageDownloadUrlToDatabase(downloadUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            userRef.child(userId).child("avatar").setValue(downloadUrl)
        }
    }
    fun getCurrentFamily() {
        userRef.child(auth.currentUser!!.uid).child("familyId")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val familyId = snapshot.getValue(String::class.java)
                    familyId?.let {
                        Log.d("get familyid", it)
                        currentFamilyId.value = it
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Get current family", error.message.toString())
                    currentFamilyId.value = null // Handle the error by passing an empty string or an appropriate value.
                }
            })
    }
    fun createFamily(){
        val users = mutableListOf<User>()
        val newFamilyRef  = familyRef.push()
        newFamilyRef.child(auth.currentUser!!.uid).setValue(true).addOnCompleteListener {
            if (it.isSuccessful) {
                val familyId = newFamilyRef.key
                val name = currentUser.value!!.name
                val email = currentUser.value!!.email
                val avatar = currentUser.value!!.avatar
                val user = User(auth.currentUser!!.uid,name, email, avatar)
                users.add(user)
                userList.value = users
                userRef.child(auth.currentUser!!.uid).child("familyId").setValue(familyId).addOnCompleteListener{it1 ->
                    if (it1.isSuccessful)
                        Log.d("Create family","Created successfully")
                }
                val messId = Random.nextInt()
                val message = Message(messId.toString(),auth.currentUser!!.uid, "Welcome to family chat",System.currentTimeMillis(), MessageType.TEXT)
                val mapMess = mapOf<String, Message>("WelcomeMessage" to message)
                val chatRoom = ChatRoom(familyId!!,ChatRoomType.FAMILY,"Family",message.content, message.time, mapMess)
                chatRef.child("FamilyChat").child(familyId).setValue(chatRoom).addOnCompleteListener{
                    it2->if (it2.isSuccessful){
                        Log.d("Create family chat", "Successfully")
                    }
                    else Log.d("Create family chat", it2.exception.toString())
                }
            }
        }
    }
    fun fetchDataUserById(userId:String):User{
        var temp = User()
        userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                temp = dataSnapshot.getValue(User::class.java)!!
                temp?.let { user.value = temp }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("get current user", databaseError.message)
            }
        })
        return temp
    }
    fun addUser(userId:String, familyId: String) {
        val currentList = userList.value!!.toMutableList()
        userRef.child(userId).child("familyId")
            .addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val famId = snapshot.getValue(String::class.java)
                    if (famId == ""){
                        familyRef.child(familyId).child(userId).setValue(true).addOnCompleteListener {
                            if (it.isSuccessful) {
                                userRef.child(userId).child("familyId").setValue(familyId).addOnCompleteListener{it1 ->
                                    if (it1.isSuccessful) {
                                        val user = fetchDataUserById(userId)
                                        Log.d("add member", user.email)
                                        currentList.add(user)
                                        userList.value = currentList
                                        adapter.submitList(currentList)
                                        Log.d("Add Member", "Added successfully")
                                        statusAdd.value = true
                                    }
                                    else{
                                        statusAdd.value = false
                                        Log.d("Add Member", it1.exception.toString())
                                    }
                                }
                            }else{
                                statusAdd.value = false
                                Log.d("Add Member", it.exception.toString())
                            }
                        }
                    }
                    else {
                        statusAdd.value = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    statusAdd.value = false
                    Log.d("Add Member", error.toString())
                }

            })
    }
    fun getUsersInFamily(familyId:String) {
        val users = mutableListOf<User>()
        familyRef.child(familyId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userIds = mutableListOf<String>()

                for (childSnapshot in dataSnapshot.children) {
                    val userId = childSnapshot.key
                    userId?.let { userIds.add(it) }
                }

                for (userId in userIds) {
                    userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            val user = dataSnapshot.getValue(User::class.java)
                            user?.let { users.add(it) }
                            if (users.size == userIds.size) {
                                userList.value = users
                                Log.d("user list", userList.value.toString())
                            }
                        }
                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
    fun removeUser(userId:String, familyId: String){
        userRef.child(userId).child("familyId").setValue("").addOnCompleteListener {
            if (it.isSuccessful) {
                familyRef.child(familyId).child("member").child(userId).removeValue()
                chatRef.child("UserChat").addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (userChatSnap in snapshot.children){
                            val id = userChatSnap.key
                            if (id != null) {
                                if (id.contains(userId)){
                                    chatRef.child("UserChat").child(id).removeValue()
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })
            }else Log.e("remove familyId in UserRef", it.exception.toString())
        }
    }
}