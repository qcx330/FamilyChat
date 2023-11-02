package com.example.familychat.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.R
import com.example.familychat.adapter.UserAdapter
import com.example.familychat.viewmodel.UserViewModel

class FamilyFragment : Fragment() {

    companion object {
        fun newInstance() = FamilyFragment()
    }

    private lateinit var viewModel: UserViewModel
    private lateinit var btnCreate : AppCompatButton
    private lateinit var btnAdd : AppCompatButton
    private lateinit var tvCreate : TextView
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_family, container, false)
        btnCreate = view.findViewById(R.id.btnCreate)
        btnAdd = view.findViewById(R.id.btnAdd)
        tvCreate = view.findViewById(R.id.tvCreate)
        recyclerView = view.findViewById(R.id.listMember)
        val adapter = UserAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        viewModel.getCurrentFamily()
        viewModel.getCurrentFamilyId().observe(viewLifecycleOwner){
            id -> if (id!= null){
                viewModel.getUsersInFamily(id)
            }
        }

        viewModel.getUserList().observe(viewLifecycleOwner) { users ->
            if (users.isEmpty()){
                btnAdd.visibility = View.GONE
                btnCreate.visibility = View.VISIBLE
                tvCreate.visibility = View.VISIBLE
            }else {
                btnAdd.visibility = View.VISIBLE
                btnCreate.visibility = View.GONE
                tvCreate.visibility = View.GONE
                adapter.submitList(users)
                Log.d("get list user", users.toString())
            }

        }

        btnCreate.setOnClickListener(){
            viewModel.createFamily()
        }
        btnAdd.setOnClickListener(){
            val showPopup = PopUpFragment()
            showPopup.show((activity as AppCompatActivity).supportFragmentManager, "showPopup")
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}