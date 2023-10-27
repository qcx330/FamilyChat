package com.example.familychat.view

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import com.example.familychat.R
import com.example.familychat.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomnav : BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Thread.sleep(3000)
        installSplashScreen()

        setContentView(R.layout.activity_main)

        bottomnav = findViewById(R.id.bottomNav)

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
}