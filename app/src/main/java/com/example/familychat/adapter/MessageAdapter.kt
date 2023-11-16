package com.example.familychat.adapter

import android.content.IntentSender.SendIntentException
import android.media.Image
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.familychat.R
import com.example.familychat.Utils
import com.example.familychat.model.Message
import com.example.familychat.model.MessageType
import com.example.familychat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.makeramen.roundedimageview.RoundedImageView

class MessageAdapter: RecyclerView.Adapter<ViewHolder>() {
    private var messList: List<Message> = emptyList()
    private var userList: List<User> = emptyList()

    private val text_received_item = 0
    private val image_received_item = 1
    private val text_sent_item = 2
    private val image_sent_item = 3

    inner class SentViewHolder(itemView:View):ViewHolder(itemView){
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val imgMess = itemView.findViewById<ImageView>(R.id.imgMessage)
    }
    inner class ReceiveViewHolder(itemView:View):ViewHolder(itemView){
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgAvatar = itemView.findViewById<RoundedImageView>(R.id.imgAvatar)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val imgMess = itemView.findViewById<ImageView>(R.id.imgMessage)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messList[position]
        return when {
            currentMessage.sender == FirebaseAuth.getInstance().currentUser?.uid -> {
                if (currentMessage.type == MessageType.TEXT)
                    return text_sent_item
                 else
                    return image_sent_item

            }
            else -> {
                if (currentMessage.type == MessageType.TEXT)
                    return text_received_item
                 else
                    return image_received_item

            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 0){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_message, parent, false)
            return ReceiveViewHolder(view)
        }
        else if (viewType == 1) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_image, parent, false)
            return ReceiveViewHolder(view)
        }
        else if (viewType == 2) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_message, parent, false)
            return SentViewHolder(view)
        }
        else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_image, parent, false)
            return SentViewHolder(view)
        }
    }
    fun submitList(newList: List<Message>) {
        messList = newList
    }
    fun submitUser(newList: List<User>) {
        userList = newList
    }
    override fun getItemCount(): Int {
        return messList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messList[position]
        Log.d("current message", currentMessage.toString())
        if (holder.javaClass == SentViewHolder::class.java){
            val viewHolder = holder as SentViewHolder
            if (currentMessage.type == MessageType.TEXT)
                viewHolder.tvMessage.text = currentMessage.content
            else Glide.with(holder.itemView).load(currentMessage.content).into(holder.imgMess)
            viewHolder.tvTime.text = Utils.formatTimestamp(currentMessage.time!!)
        }else{
            val viewHolder = holder as ReceiveViewHolder
            Log.d("Userlistadapter", userList.toString())
            val sender = userList.firstOrNull { it.id == currentMessage.sender } as User
            Log.d("sender", sender.toString())
            if (currentMessage.type == MessageType.TEXT)
                viewHolder.tvMessage.text = currentMessage.content
            else Glide.with(holder.itemView).load(currentMessage.content).into(holder.imgMess)
            viewHolder.tvName.text = sender.name
            viewHolder.tvTime.text = Utils.formatTimestamp(currentMessage.time!!)
            if (sender.avatar != "")
                Glide.with(holder.itemView).load(sender.avatar).into(holder.imgAvatar)
        }
    }
}