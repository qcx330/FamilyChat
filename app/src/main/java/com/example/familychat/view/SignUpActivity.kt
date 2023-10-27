package com.example.familychat.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.example.familychat.databinding.ActivitySignupBinding
import com.example.familychat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.btnSignup.setOnClickListener() {
            val name = binding.edtName.text.toString()
            val email = binding.edtEmail.text.toString()
            val password = binding.edtPassword.text.toString()
            val confirmPass = binding.edtConfirmPass.text.toString()
            val user = User(name, email,"")
            Log.d("ADD", "name: $name")
            Log.d("ADD", "email: $email")
            if (confirmPass == password) {
                if (isEmailValid(email)) {
                    if (name.isNotEmpty()) {
                        registerUser(user, email, password)
                    } else Toast.makeText(this, "Name cant be empty", Toast.LENGTH_SHORT).show()

                } else Toast.makeText(this, "Email is invalid", Toast.LENGTH_SHORT).show()
            } else Toast.makeText(this, "Confirm password is wrong", Toast.LENGTH_SHORT).show()
    }

}

private fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

private fun registerUser(user:User, email:String, password:String) {
    auth.createUserWithEmailAndPassword(email,password)
        .addOnCompleteListener(this) {
            if (it.isSuccessful) {
                var userFB: FirebaseUser? = auth.currentUser
                var userId: String = userFB!!.uid

                databaseReference = FirebaseDatabase.getInstance()
                    .getReference("User")
                    .child(userId)
                databaseReference.setValue(user).addOnCanceledListener(this) {
                    if (it.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                    }
                }
            }
        }
        .addOnCanceledListener() { Log.d("ADD", "Fail") }
}
}