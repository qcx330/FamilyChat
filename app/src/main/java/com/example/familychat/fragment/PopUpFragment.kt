package com.example.familychat.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.familychat.R
import com.example.familychat.viewmodel.UserViewModel
import com.google.android.material.snackbar.Snackbar
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
        viewModel.getCurrentFamily()
        btnAdd.setOnClickListener(){
            viewModel.getCurrentFamilyId().observe(viewLifecycleOwner){
                    id -> if (id!= null){
                viewModel.addUser(edtUserId.text.toString(), id)
                viewModel.getStatusAdd().observe(viewLifecycleOwner){
                    status -> if (status){
                        dismiss()
                    }
                    else Snackbar.make(view, "User is already in another family", Snackbar.LENGTH_SHORT).view
                }
            }
                else Log.d("add member", "id null")
            }
        }
        return view
    }

}