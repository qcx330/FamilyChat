package com.example.familychat.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.familychat.R
import com.example.familychat.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private lateinit var bottomnav : BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        bottomnav = binding.bottomNav
        var frame = binding.frameLayout

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
    fun replaceFragment(fragment:Fragment){
        supportFragmentManager.beginTransaction().replace(R.id.frameLayout, fragment).commit()
    }
}