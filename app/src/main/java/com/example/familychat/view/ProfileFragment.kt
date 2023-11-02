package com.example.familychat.view

import android.app.Activity.RESULT_OK
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.familychat.R
import com.example.familychat.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.makeramen.roundedimageview.RoundedImageView

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: UserViewModel
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnCopy: AppCompatButton
    private lateinit var btnLogout: AppCompatButton
    private lateinit var imgAvatar : RoundedImageView
    private val auth = FirebaseAuth.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        btnLogout = view.findViewById(R.id.btnLogout)
        tvName = view.findViewById(R.id.tvName)
        tvEmail = view.findViewById(R.id.tvEmail)
        btnCopy = view.findViewById(R.id.btnCopy)
        imgAvatar = view.findViewById(R.id.imgAvatar)

        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                tvName.text = user.name
            }
        }

        tvEmail.text = auth.currentUser!!.email
        btnCopy.text = auth.currentUser!!.uid
        btnCopy.setOnClickListener(){
            val text = btnCopy.text
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("userId", text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        btnLogout.setOnClickListener(){
            auth.signOut()
            startActivity(Intent(context, SignInActivity::class.java))
        }
        imgAvatar.setOnClickListener(){

        }

        return view
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}