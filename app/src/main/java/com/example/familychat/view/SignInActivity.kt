package com.example.familychat.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.familychat.R
import com.example.familychat.databinding.ActivitySigninBinding
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySigninBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySigninBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSignin.setOnClickListener(){
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()){
                signIn(email, password)
            }
            else Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT)
        }
        binding.btnSignUp.setOnClickListener(){
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    private fun signIn(email:String, password:String){
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful)
                startActivity(Intent(this, MainActivity::class.java))
            else {
                Log.d("Sign in", it.exception.toString())
                binding.layoutEmail.error = "Sign in information was incorrect"
                binding.layoutPassword.error = "Sign in information was incorrect"
                Toast.makeText(this, "Check log in information again", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}