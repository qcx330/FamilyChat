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
    private val userList = MutableLiveData<List<User>>()
    val currentFamilyId = MutableLiveData<String>()
    val adapter = UserAdapter()

    private val currentUserLiveData = MutableLiveData<User>()

    val currentUser: LiveData<User>
        get() = currentUserLiveData

    //Firebase
    private val userRef = FirebaseDatabase.getInstance().getReference("User")
    private val familyRef = FirebaseDatabase.getInstance().getReference("Family")
    private val auth = FirebaseAuth.getInstance()

    init{
        userList.value = mutableListOf()
        loadCurrentUser()
    }
    fun getUserList(): LiveData<List<User>> {
        return userList
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
        val users = mutableListOf<User>()
        val newFamilyRef  = familyRef.push()
        newFamilyRef.child(auth.currentUser!!.uid).setValue(true).addOnCompleteListener {
            if (it.isSuccessful) {
                val familyId = newFamilyRef.key
                val name = currentUser.value!!.name
                val email = currentUser.value!!.email
                val avatar = currentUser.value!!.avatar
                val user = User(name, email, avatar)
                users.add(user)
                userList.value = users
                userRef.child(auth.currentUser!!.uid).child("familyId").setValue(familyId).addOnCompleteListener{it1 ->
                    if (it1.isSuccessful)
                        Log.d("Create family","Created successfully")
                }
            }
        }
    }

//    fun addUser(userId:String) {
//        val familyId = getCurrentFamily()
//        familyRef.child(familyId).child(userId).setValue(true).addOnCompleteListener {
//            if (it.isSuccessful) {
////                val name = currentUser.value!!.name
////                val email = currentUser.value!!.email
////                val avatar = currentUser.value!!.avatar
////                val user = User(name, email, avatar)
////                userList.value?.add(user)
//                userRef.child(userId).child("familyId").setValue(familyId).addOnCompleteListener{it1 ->
//                    if (it1.isSuccessful)
//                        Log.d("Add Member","Added successfully")
//                }
//            }
//        }
////        val currentList = userList.value
////        currentList?.add(userId)
////        userList.value = currentList!!
//    }
    fun getUsersInFamily() {
        val users = mutableListOf<User>()

        familyRef.child("-NiAWqNF4EkA1E9vkI2v").addListenerForSingleValueEvent(object : ValueEventListener {
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