package com.example.familychat.viewmodel

import android.util.Log
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
    private val currentUserLiveData = MutableLiveData<User>()

    val currentUser :LiveData<User>
        get() = currentUserLiveData

    //Firebase
    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val familyRef = FirebaseDatabase.getInstance().getReference("Family")
    private val auth = FirebaseAuth.getInstance()

    init{
        userList.value = mutableListOf()
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            currentUserLiveData.value = User(
                user!!.displayName!!,
                user.email!!,
                user.photoUrl.toString())
        }
    }
    fun getUserList():LiveData<MutableList<User>>{
        return userList
    }
    fun getCurrentFamily():String {
        var familyId:String =""
        userRef.child(auth.currentUser!!.uid).child("familyId").addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                familyId = snapshot.getValue(String::class.java)!!
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Get current family", error.message.toString())
            }
        })
        return familyId
    }
    fun createFamily(){
        val newFamilyRef  = familyRef.push()
        newFamilyRef.child(auth.currentUser!!.uid).setValue(true).addOnCompleteListener {
            if (it.isSuccessful) {
                val familyId = newFamilyRef.key
                val name = currentUser.value!!.name
                val email = currentUser.value!!.email
                val avatar = currentUser.value!!.avatar
                val user = User(name, email, avatar)
                userList.value?.add(user)
                userRef.child(auth.currentUser!!.uid).child("familyId").setValue(familyId).addOnCompleteListener{it1 ->
                    if (it1.isSuccessful)
                        Log.d("Create family","Created successfully")
                }
            }
        }
    }

    fun addUser(userId:String) {
        val familyId = getCurrentFamily()
        familyRef.child(familyId).child(userId).setValue(true).addOnCompleteListener {
            if (it.isSuccessful) {
//                val name = currentUser.value!!.name
//                val email = currentUser.value!!.email
//                val avatar = currentUser.value!!.avatar
//                val user = User(name, email, avatar)
//                userList.value?.add(user)
                userRef.child(userId).child("familyId").setValue(familyId).addOnCompleteListener{it1 ->
                    if (it1.isSuccessful)
                        Log.d("Add Member","Added successfully")
                }
            }
        }
//        val currentList = userList.value
//        currentList?.add(userId)
//        userList.value = currentList!!
    }

    fun removeMember(position: Int) {
        val currentList = userList.value
        if (position >= 0 && position < currentList?.size ?: 0) {
            currentList?.removeAt(position)
            userList.value = currentList!!
        }
    }
}