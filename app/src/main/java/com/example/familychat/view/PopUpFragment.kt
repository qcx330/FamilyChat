package com.example.familychat.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.familychat.R
import com.example.familychat.viewmodel.UserViewModel
import com.google.android.material.textfield.TextInputEditText

class PopUpFragment : DialogFragment() {
    private lateinit var viewModel: UserViewModel
    private lateinit var btnAdd : AppCompatButton
    private lateinit var edtUserId : TextInputEditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pop_up, container, false)

        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        btnAdd = view.findViewById(R.id.btnAdd)
        edtUserId = view.findViewById(R.id.edtUserId)

        btnAdd.setOnClickListener(){
            viewModel.getCurrentFamilyId().observe(viewLifecycleOwner){
                    id -> if (id!= null){
                        Log.d("add member", id)
                viewModel.addUser(edtUserId.text.toString(), id)
            }
                else Log.d("add member", "id null")
            }
            dismiss()
        }
        return view
    }

}