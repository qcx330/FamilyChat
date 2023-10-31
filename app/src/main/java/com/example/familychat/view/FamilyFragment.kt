package com.example.familychat.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import com.example.familychat.R
import com.example.familychat.viewmodel.UserViewModel

class FamilyFragment : Fragment() {

    companion object {
        fun newInstance() = FamilyFragment()
    }

    private lateinit var viewModel: UserViewModel
    private lateinit var btnCreate : AppCompatButton
    private lateinit var btnAdd : AppCompatButton
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_family, container, false)
        btnCreate = view.findViewById(R.id.btnCreate)
        btnAdd = view.findViewById(R.id.btnAdd)
        if (viewModel.getUserList().value == null)
        {
            btnCreate.visibility = View.GONE
        }
        else btnAdd.visibility = View.GONE
        return view

        btnCreate.setOnClickListener(){
            viewModel.createFamily()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}