package com.example.familychat.view

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.DialogFragment
import com.example.familychat.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class ChangePwFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private val auth = FirebaseAuth.getInstance()
    lateinit var btnChange: AppCompatButton
    lateinit var edtCurrentPw: TextInputEditText
    lateinit var layoutCurrentPw: TextInputLayout
    lateinit var edtNewPw: TextInputEditText
    lateinit var layoutNewPw: TextInputLayout
    lateinit var edtConfirmPw: TextInputEditText
    lateinit var layoutConfirmPw: TextInputLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_change_pw, container, false)
        btnChange = view.findViewById(R.id.btnChange)
        edtCurrentPw = view.findViewById(R.id.edtCurrentPw)
        edtNewPw = view.findViewById(R.id.edtNewPw)
        edtConfirmPw = view.findViewById(R.id.edtConfirmNewPw)
        layoutCurrentPw = view.findViewById(R.id.layoutCurrentPw)
        layoutNewPw = view.findViewById(R.id.layoutNewPw)
        layoutConfirmPw = view.findViewById(R.id.layoutConfirmPw)

        btnChange.setOnClickListener() {
            val credential = EmailAuthProvider.getCredential(
                auth.currentUser!!.email!!,
                edtCurrentPw.text.toString()
            )
            auth.currentUser!!.reauthenticate(credential)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        if (edtNewPw.text.toString() == edtConfirmPw.text.toString()) {
                            auth.currentUser!!.updatePassword(edtNewPw.text.toString())
                            Toast.makeText(context, "Change password successfully", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                        else {
                            layoutConfirmPw.error = "Password and confirm password is not the same"
                            Toast.makeText(context, "Confirm password was incorrect", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        layoutCurrentPw.error = "Check your current password again"
                        Toast.makeText(context, "Current password was incorrect", Toast.LENGTH_SHORT).show()
                    }
                }

        }
        return view
    }

}