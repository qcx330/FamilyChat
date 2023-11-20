package com.example.familychat.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.familychat.R
import com.example.familychat.fragment.FamilyFragment
import com.example.familychat.fragment.MessageFragment
import com.example.familychat.fragment.ProfileFragment
import com.example.familychat.notification.FirebaseService
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService

class MainActivity : AppCompatActivity() {
    private lateinit var bottomnav : BottomNavigationView
    lateinit var auth : FirebaseAuth
    lateinit var db : FirebaseDatabase
    var token :String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        Thread.sleep(1000)
        setContentView(R.layout.activity_main)

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnCompleteListener() { task ->
            if (task.isSuccessful) {
                FirebaseService.token = task.result
                Log.i("fcmToken", task.result)
            }
        }
            .addOnFailureListener {
                Log.e("Error get fcmToken", it.message.toString())
            }

        bottomnav = findViewById(R.id.bottomNav)
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        bottomnav.setOnItemSelectedListener { item ->
            when(item.itemId){
                R.id.message ->{
                    replaceFragment(MessageFragment())
                    true
                }
                R.id.family ->{
                    replaceFragment(FamilyFragment())
                    true
                }
                R.id.profile ->{
                    replaceFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        replaceFragment(MessageFragment())

    }
    private fun replaceFragment(fragment:Fragment){
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_out_right,R.anim.slide_in_left)
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
    private fun getFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener(){
            task -> if (task.isSuccessful){
                val token = task.result
                val updates = hashMapOf<String, Any>("fcmToken" to token)

                db.reference.child("User").child(auth.currentUser!!.uid).updateChildren(updates).addOnSuccessListener(){
                    Log.i("fcmToken", token)
                }
                    .addOnFailureListener {
                        Log.i("Error updating fcmToken", it.message.toString())
                    }
            }

        }
    }
}