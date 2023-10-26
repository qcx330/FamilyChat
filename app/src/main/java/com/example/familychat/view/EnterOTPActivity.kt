package com.example.familychat.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.familychat.databinding.ActivityEnterOtpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit


class EnterOTPActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEnterOtpBinding
    private lateinit var phoneNumber: String
    private var mAuth = FirebaseAuth.getInstance()
    lateinit var verificationCode :String
    private lateinit var resendingToken : PhoneAuthProvider.ForceResendingToken
    var timeout : Long = 60L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEnterOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        phoneNumber = intent.getStringExtra("phone").toString()
        Log.d("Intent", "phone: $phoneNumber")

        sendOtp(phoneNumber, false)

        binding.btnEnter.setOnClickListener(){
            val enteredOtp = binding.edtOtp.text.toString()
            Log.d("OTP","Entered otp: $enteredOtp")
            val credential = PhoneAuthProvider.getCredential(verificationCode, enteredOtp)
            Log.d("OTP","Verification code: $verificationCode")
            signIn(credential)
        }
    }

    private fun sendOtp(phone:String, isResend: Boolean){
        startResendTimer()
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            signIn(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(applicationContext, "OTP verification failed", Toast.LENGTH_SHORT).show()
            if (e is FirebaseAuthInvalidCredentialsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            } else if (e is FirebaseTooManyRequestsException) {
                Log.d("TAG", "onVerificationFailed: ${e.toString()}")
            }
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            verificationCode = verificationId
            resendingToken = token
            Toast.makeText(applicationContext, "OPT sent successfully", Toast.LENGTH_SHORT).show()
        }
    }

    fun signIn(phoneAuthCredential: PhoneAuthCredential){
        mAuth.signInWithCredential(phoneAuthCredential)
            .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                startActivity(Intent(this, MainActivity::class.java))
                Toast.makeText(applicationContext, "Signed in", Toast.LENGTH_SHORT).show()
            } else {
                Log.d("TAG", "signInWithPhoneAuthCredential: ${task.exception.toString()}")
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    Log.d("TAG", "The verification code entered was invalid")
                }
            }
        }
    }

    private fun startResendTimer(){
        binding.btnResend.isEnabled = false
        var timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                timeout--
                if (timeout <= 0) {
                    timeout = 60L
                    timer.cancel()
                    runOnUiThread {
                        binding.btnResend.isEnabled = true
                    }
                }
            }
        }, 0, 1000)
    }
}