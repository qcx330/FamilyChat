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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.R
import com.example.familychat.adapter.ChatAdapter
import com.example.familychat.adapter.RvInterface
import com.example.familychat.activity.ChatActivity
import com.example.familychat.viewmodel.ChatViewModel
import com.example.familychat.viewmodel.MessageViewModel
import com.example.familychat.viewmodel.UserViewModel

class MessageFragment : Fragment() {

    companion object {
        fun newInstance() = MessageFragment()
    }

    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var messageViewModel: MessageViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvMessage: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)

        recyclerView = view.findViewById(R.id.listMessage)

        tvMessage = view.findViewById(R.id.tvMessage)
        val adapter = ChatAdapter(object : RvInterface{
            override fun OnClickItem(pos: Int) {
                chatViewModel.getChatRoomList().observe(viewLifecycleOwner){
                        it ->if (it != null){
                    val intent = Intent(context, ChatActivity::class.java)
                    chatViewModel.setChatRoomId(it[pos].roomId)
                    startActivity(intent)
                }else Log.d("chat id intent", "null")

                }

            }
        })
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
        userViewModel.getCurrentFamily()
        userViewModel.getCurrentFamilyId().observe(viewLifecycleOwner){
                id -> if (id!= ""){
                    Log.d("familyId", id!!)
            chatViewModel.retrieveFamilyChat(id) }
        }
        chatViewModel.otherUser.observe(viewLifecycleOwner) { otherUser ->
            adapter.updateOtherUserDetails(otherUser.id!!, otherUser)}
        chatViewModel.retrieveUserChat()
        chatViewModel.getChatRoomList().observe(viewLifecycleOwner){
            it ->if (it != null) {
            adapter.submitList(it)
            tvMessage.visibility = View.GONE
        }
            else {
                tvMessage.visibility = View.VISIBLE
        }

        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // TODO: Use the ViewModel
    }

}