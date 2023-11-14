package com.example.familychat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familychat.R
import com.example.familychat.Utils
import com.example.familychat.model.ChatRoom
import com.example.familychat.model.ChatRoomType
import com.example.familychat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.makeramen.roundedimageview.RoundedImageView

class ChatAdapter(val onItemClick: RvInterface) :RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private var chatRoomList: List<ChatRoom> = emptyList()
    private val otherUserDetailsMap: MutableMap<String, User> = mutableMapOf()
    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    inner class ChatViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val imgAvatar = itemView.findViewById<RoundedImageView>(R.id.imgAvatar)
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
    }
    fun submitList(newList: List<ChatRoom>) {
        chatRoomList = newList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_chat, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int {
        return chatRoomList.size
    }
    fun updateOtherUserDetails(otherId:String, otherUser:User) {
        otherUserDetailsMap[otherId] = otherUser
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatRow = chatRoomList[position]

        if (chatRow.roomType == ChatRoomType.FAMILY){
            holder.tvName.text = chatRow.roomName
        }
        else {
            val otherUser = chatRow.member!!.firstOrNull { it.id != currentUserId } as User
                holder.tvName.text = otherUser.name
                if (otherUser.avatar != "")
                    Glide.with(holder.itemView).load(otherUser.avatar).into(holder.imgAvatar)
            }

        holder.tvMessage.text = chatRow.lastMessage
        holder.tvTime.text = Utils.formatTimestamp(chatRow.timestamp!!)
        holder.itemView.setOnClickListener(){
            onItemClick.OnClickItem(position)
        }
    }
}