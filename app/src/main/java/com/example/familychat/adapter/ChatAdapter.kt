package com.example.familychat.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.familychat.R
import com.example.familychat.Utils
import com.example.familychat.model.ChatRoom
import com.example.familychat.model.ChatRoomType
import com.example.familychat.model.Message
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
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = chatRoomList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                diffCallback.areItemsTheSame(chatRoomList[oldItemPosition], newList[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                diffCallback.areContentsTheSame(chatRoomList[oldItemPosition], newList[newItemPosition])
        })

        chatRoomList= newList
        notifyPositionChanges(diffResult)
    }

    private fun notifyPositionChanges(diffResult: DiffUtil.DiffResult) {
        val movedItems = mutableListOf<Pair<Int, Int>>()
        diffResult.dispatchUpdatesTo(object : ListUpdateCallback {
            override fun onInserted(position: Int, count: Int) {
            }

            override fun onRemoved(position: Int, count: Int) {
            }

            override fun onMoved(fromPosition: Int, toPosition: Int) {
                movedItems.add(fromPosition to toPosition)
            }

            override fun onChanged(position: Int, count: Int, payload: Any?) {
            }
        })
        movedItems.sortBy { (fromPosition, _) ->
            chatRoomList[fromPosition].timestamp
        }
        movedItems.forEach { (fromPosition, toPosition) ->
            notifyItemMoved(fromPosition, toPosition)
        }
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
    private val diffCallback = object : DiffUtil.ItemCallback<ChatRoom>() {
        override fun areItemsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem.roomId == newItem.roomId
        }

        override fun areContentsTheSame(oldItem: ChatRoom, newItem: ChatRoom): Boolean {
            return oldItem == newItem
        }
    }

}