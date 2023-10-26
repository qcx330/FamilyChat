package com.example.familychat.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import com.example.familychat.R
import com.example.familychat.databinding.ActivityEnterOtpBinding
import com.example.familychat.databinding.ActivitySinginBinding
import com.hbb20.CountryCodePicker

class SingInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySinginBinding
    private lateinit var edtPhoneNumber : EditText
    private lateinit var pickerCountry : CountryCodePicker


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        edtPhoneNumber = binding.edtPhoneNumber
        pickerCountry = binding.pickerCountryCode
        pickerCountry.registerCarrierNumberEditText(edtPhoneNumber)

        binding.btnSignin.setOnClickListener(){
            if (!pickerCountry.isValidFullNumber){
                edtPhoneNumber.error = "Phone number is not valid"
            }
            else {
                val intent = Intent(this, EnterOTPActivity::class.java)
                intent.putExtra("phone", pickerCountry.fullNumberWithPlus)
                startActivity(intent)
            }
        }
    }
}