package com.example.familychat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.R
import com.example.familychat.model.ChatRoom
import com.makeramen.roundedimageview.RoundedImageView

class ChatAdapter :RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private var chatRoomList: List<ChatRoom> = emptyList()
    inner class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgAvatar = itemView.findViewById<RoundedImageView>(R.id.imgAvatar)
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatRoomList.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatRow = chatRoomList[position]
    }
}