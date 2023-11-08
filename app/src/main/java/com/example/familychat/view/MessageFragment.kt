package com.example.familychat.view

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.R
import com.example.familychat.adapter.ChatAdapter
import com.example.familychat.viewmodel.ChatViewModel
import com.example.familychat.viewmodel.UserViewModel

class MessageFragment : Fragment() {

    companion object {
        fun newInstance() = MessageFragment()
    }

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)
        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        recyclerView = view.findViewById(R.id.listMessage)
        val adapter = ChatAdapter()
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        userViewModel.getCurrentFamily()
        userViewModel.getCurrentFamilyId().observe(viewLifecycleOwner){
                id -> if (id!= null){
            chatViewModel.retrieveFamilyChat(id) }
        }
        chatViewModel.getChatRoomList().observe(viewLifecycleOwner){
            it -> adapter.submitList(it)
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}