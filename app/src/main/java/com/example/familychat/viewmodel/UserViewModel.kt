package com.example.familychat.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.adapter.UserAdapter
import com.example.familychat.model.ChatRoom
import com.example.familychat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class UserViewModel (): ViewModel() {
    private val userList = MutableLiveData<List<User>>()
    val currentFamilyId = MutableLiveData<String?>()
    val adapter = UserAdapter()

    private val currentUserLiveData = MutableLiveData<User>()

    val currentUser: LiveData<User>
        get() = currentUserLiveData

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
    fun getUserList(): LiveData<List<User>> {
        return userList
    }
    fun getCurrentFamilyId():LiveData<String?>{
        return currentFamilyId
    }
    private fun loadCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid

            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let { currentUserLiveData.value = it }
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
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                val chatRoom = ChatRoom(familyId!!, "Family", "Welcome to family chat", System.currentTimeMillis())
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
    fun addUser(userId:String, familyId: String) {
        val currentList = userList.value!!.toMutableList()
        familyRef.child(familyId).child(userId).setValue(true).addOnCompleteListener {
            if (it.isSuccessful) {
                userRef.child(userId).child("familyId").setValue(familyId).addOnCompleteListener{it1 ->
                    if (it1.isSuccessful) {
                        val user = fetchDataUserById(userId)
                        Log.d("add member", user.email)
                        currentList.add(user)
                        userList.value = currentList
                        adapter.submitList(currentList)
                        adapter.notifyDataSetChanged()
                        Log.d("Add Member", "Added successfully")
                    }
                    else{
                        Log.d("Add Member", it1.exception.toString())
                    }
                }
            }else{
                Log.d("Add Member", it.exception.toString())
            }
        }
    }
    fun getUsersInFamily(familyId:String) {
        val users = mutableListOf<User>()

        familyRef.child(familyId).addListenerForSingleValueEvent(object : ValueEventListener {
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

                            // Check if all user details have been fetched
                            if (users.size == userIds.size) {
                                userList.value = users
                                Log.d("user list", userList.value.toString())
                            }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            // Handle database error
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}