package com.example.familychat.fragment

import android.content.Intent
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
import com.example.familychat.adapter.RvInterface
import com.example.familychat.adapter.UserAdapter
import com.example.familychat.activity.ChatActivity
import com.example.familychat.viewmodel.ChatViewModel
import com.example.familychat.viewmodel.UserViewModel

class FamilyFragment : Fragment() {

    companion object {
        fun newInstance() = FamilyFragment()
    }

    private lateinit var userViewModel: UserViewModel
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var btnCreate : AppCompatButton
    private lateinit var btnAdd : AppCompatButton
    private lateinit var tvCreate : TextView
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_family, container, false)
        btnCreate = view.findViewById(R.id.btnCreate)
        btnAdd = view.findViewById(R.id.btnAdd)
        tvCreate = view.findViewById(R.id.tvCreate)
        recyclerView = view.findViewById(R.id.listMember)


        val adapter = UserAdapter(object : RvInterface{
            override fun OnClickItem(pos: Int) {
                userViewModel.getUserList().observe(viewLifecycleOwner){
                    it ->if (it!= null){
                        chatViewModel.getChatRoom(it[pos].id!!)
                        val intent = Intent(context, ChatActivity::class.java)
                        intent.putExtra("id", it[pos].id)
                        startActivity(intent)
                    }
                    else Log.e("intent user id", "null")
                }
            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        userViewModel.getCurrentFamily()
        userViewModel.getCurrentFamilyId().observe(viewLifecycleOwner){
            id -> if (id!= null){
            userViewModel.getUsersInFamily(id)
            }
        }

        userViewModel.getUserList().observe(viewLifecycleOwner) { users ->
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
            userViewModel.createFamily()
        }
        btnAdd.setOnClickListener(){
            val showPopup = PopUpFragment()
            showPopup.show((activity as AppCompatActivity).supportFragmentManager, "showPopup")
        }

        return view
    }

}