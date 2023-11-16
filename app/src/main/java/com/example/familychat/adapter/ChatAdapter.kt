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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.makeramen.roundedimageview.RoundedImageView

class ChatAdapter(val onItemClick: RvInterface) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {
    private var chatRoomList: List<ChatRoom> = emptyList()
    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    private val database = FirebaseDatabase.getInstance()
    private val userRef = database.getReference("User")
    private val chatRef = database.getReference("Chat")

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatRow = chatRoomList[position]

        if (chatRow.roomType == ChatRoomType.FAMILY) {
            holder.tvName.text = chatRow.roomName
        } else {
            chatRef.child("UserChat")
                .child(chatRow.roomId!!)
                .child("member").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for (childSnapshot in snapshot.children) {
                            val userId = childSnapshot.value
                            userId?.let {
                                if (userId != currentUserId) {
                                    userRef.child(userId.toString())
                                        .addListenerForSingleValueEvent(object :
                                            ValueEventListener {
                                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                                val user = userSnapshot.getValue(User::class.java)
                                                user?.let {
                                                    holder.tvName.text = user.name
                                                    if (user.avatar != "")
                                                        Glide.with(holder.itemView)
                                                            .load(user.avatar)
                                                            .into(holder.imgAvatar)
                                                }
                                            }

                                            override fun onCancelled(error: DatabaseError) {
                                                // Handle the error
                                                Log.e(
                                                    "YourRepository",
                                                    "Error getting user details: $error"
                                                )
                                            }
                                        })
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "YourRepository",
                            "Error getting user id: $error"
                        )
                    }

                })
        }

        holder.tvMessage.text = chatRow.lastMessage
        holder.tvTime.text = Utils.formatTimestamp(chatRow.timestamp!!)
        holder.itemView.setOnClickListener() {
            onItemClick.OnClickItem(position)
        }
    }
}