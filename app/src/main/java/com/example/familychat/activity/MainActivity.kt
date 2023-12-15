package com.example.familychat.activity

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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNav : BottomNavigationView
    lateinit var auth : FirebaseAuth
    private lateinit var db : FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        Thread.sleep(1000)
        setContentView(R.layout.activity_main)


        if (intent.extras != null){
            val chatId = intent.extras!!.getString("chatId")
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chatId)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }

        bottomNav = findViewById(R.id.bottomNav)
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()

        bottomNav.setOnItemSelectedListener { item ->
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

        getFCMToken()
    }
    private fun replaceFragment(fragment:Fragment){
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_out_right,R.anim.slide_in_left)
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
    private fun getFCMToken(){
        FirebaseMessaging.getInstance().token.addOnCompleteListener{
            task -> if (task.isSuccessful){
                val token = task.result
                Log.i("fcmToken", token)
                db.reference.child("User").child(auth.currentUser!!.uid).child("token").setValue(token).addOnSuccessListener{
                    Log.i("fcmToken", token)
                }
                    .addOnFailureListener {
                        Log.i("Error updating fcmToken", it.message.toString())
                    }
            }

        }
    }


}