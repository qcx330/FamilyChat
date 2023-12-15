package com.example.familychat.fragment

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.familychat.R
import com.example.familychat.activity.SignInActivity
import com.example.familychat.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.makeramen.roundedimageview.RoundedImageView

class ProfileFragment : Fragment() {

    private lateinit var viewModel: UserViewModel
    private lateinit var edtName: EditText
    private lateinit var tvEmail: TextView
    private lateinit var btnCopy: AppCompatButton
    private lateinit var btnLogout: AppCompatButton
    private lateinit var btnChangePw: AppCompatButton
    private lateinit var imgAvatar: RoundedImageView
    private lateinit var btnChangeName: AppCompatButton

    private val auth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        btnLogout = view.findViewById(R.id.btnLogout)
        edtName = view.findViewById(R.id.edtName)
        tvEmail = view.findViewById(R.id.tvEmail)
        btnCopy = view.findViewById(R.id.btnCopy)
        btnChangePw = view.findViewById(R.id.btnChangePw)
        imgAvatar = view.findViewById(R.id.imgAvatar)
        btnChangeName = view.findViewById(R.id.btnChangeName)
        var name = ""
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            edtName.text = user.name.toEditable()
            name = user.name
            if (user.avatar != "")
                Glide.with(this).load(user.avatar).into(imgAvatar)
            edtName.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString() == name)
                        btnChangeName.visibility = View.GONE
                    else btnChangeName.visibility = View.VISIBLE
                }
            })
        }

        btnChangeName.setOnClickListener() {
            viewModel.changeName(edtName.text.toString())
            Toast.makeText(context, "Changed", Toast.LENGTH_SHORT).show()
            btnChangeName.visibility = View.GONE
        }
        tvEmail.text = auth.currentUser!!.email
        btnCopy.text = auth.currentUser!!.uid
        btnCopy.setOnClickListener() {
            val text = btnCopy.text
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("userId", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        btnLogout.setOnClickListener() {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener() {
                if (it.isSuccessful) {
                    auth.signOut()
                    val intent = Intent(context, SignInActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                else Log.e("delete token", it.exception.toString())
            }
        }
        btnChangePw.setOnClickListener() {
            val showDialog = ChangePwFragment()
            showDialog.show((activity as AppCompatActivity).supportFragmentManager, "changepw")
        }
        imgAvatar.setOnClickListener() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            Glide.with(this).load(data?.data).into(imgAvatar)
            data?.data?.let { viewModel.setUserAvatar(it) }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

}