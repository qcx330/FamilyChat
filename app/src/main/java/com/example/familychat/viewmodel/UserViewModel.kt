package com.example.familychat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserViewModel : ViewModel() {
    private val userList = MutableLiveData<MutableList<User>>()
    private val user = MutableLiveData<User>()

    //Firebase
    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val familyRef = FirebaseDatabase.getInstance().getReference("Family")
    private val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    init{
        userList.value = mutableListOf()
    }
    fun getUserList():LiveData<MutableList<User>>{
        return userList
    }

//    fun getCurrentUser():LiveData<User>{
//        val current = FirebaseAuth.getInstance().currentUser
//        val email = current!!.email.toString()
//        val name = current!!.displayName.toString()
//        val avatar = current!!.photoUrl
//        val user = (User(name, email, avatar.toString().toInt()))
//
//        return MutableLiveData<User>().value = user
//    }

    fun addUser(userId:String) {
        userRef.child(userId).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
//        val currentList = userList.value
//        currentList?.add(userId)
//        userList.value = currentList!!
    }

    fun removeBook(position: Int) {
        val currentList = userList.value
        if (position >= 0 && position < currentList?.size ?: 0) {
            currentList?.removeAt(position)
            userList.value = currentList!!
        }
    }
}