package com.example.familychat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.familychat.adapter.UserAdapter
import com.example.familychat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserViewModel : ViewModel() {
    private val userList = MutableLiveData<MutableList<User>>()
    private val currentUserLiveData = MutableLiveData<User>()
    val adapter = UserAdapter()
    val currentFamilyId = MutableLiveData<String>()

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
    fun setCurrentFamily() {
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
                    currentFamilyId.value = "" // Handle the error by passing an empty string or an appropriate value.
                }
            })
    }
    fun createFamily(){
        val newFamilyRef  = familyRef.push()
        newFamilyRef.child(auth.currentUser!!.uid).setValue(true).addOnCompleteListener {
            if (it.isSuccessful) {
                val familyId = newFamilyRef.key
                val name = currentUser.value!!.name
                val email = currentUser.value!!.email
                val avatar = currentUser.value!!.avatar

                userRef.child(auth.currentUser!!.uid).child("familyId").setValue(familyId).addOnCompleteListener{it1 ->
                    if (it1.isSuccessful) {
                        val user = User(name, email, avatar)
                        userList.value?.add(user)
                        Log.d("Create family", "Created successfully")
                    }else {
                        Log.d("Create family", it1.exception.toString())
                    }
                }
            }
            else {
                Log.d("Create family", it.exception.toString())
            }
        }
    }

    fun removeMember(position: Int) {
        val currentList = userList.value
        if (position >= 0 && position < currentList?.size ?: 0) {
            currentList?.removeAt(position)
            userList.value = currentList!!
        }
    }
    fun getUsersInFamily(familyId: String) {
        familyRef.child(familyId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userIds = mutableListOf<String>()

                for (childSnapshot in dataSnapshot.children) {
                    val userId = childSnapshot.key
                    userId?.let { userIds.add(it) }
                }
                fetchUserDetails(userIds)
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun fetchUserDetails(userIds: List<String>) {

        for (userId in userIds) {
            userRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val user = dataSnapshot.getValue(User::class.java)
                    user?.let { userList.value?.add(it)
                        Log.d("fetchUserDetails", "User added: ${it.name}")}
                    if (userList.value?.size == userIds.size) {
                        adapter.submitList(userList.value!!)
                        Log.d("fetchUserDetails", "Adapter updated with ${userList.value?.size} users.")

                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("fetchUserDetails", "Database error: ${databaseError.message}")
                }
            })
        }
    }
}