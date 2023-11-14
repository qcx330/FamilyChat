package com.example.familychat.adapter

import android.content.IntentSender.SendIntentException
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.familychat.R
import com.example.familychat.Utils
import com.example.familychat.model.Message
import com.example.familychat.model.User
import com.google.firebase.auth.FirebaseAuth
import com.makeramen.roundedimageview.RoundedImageView

class MessageAdapter: RecyclerView.Adapter<ViewHolder>() {
    private var messList: List<Message> = emptyList()
    private var userList: List<User> = emptyList()

    private val received_item = 1
    private val sent_item = 0

    inner class SentViewHolder(itemView:View):ViewHolder(itemView){
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
    }
    inner class ReceiveViewHolder(itemView:View):ViewHolder(itemView){
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgAvatar = itemView.findViewById<RoundedImageView>(R.id.imgAvatar)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messList[position]
        return if (FirebaseAuth.getInstance().currentUser!!.uid.equals(currentMessage.sender))
            sent_item
        else received_item
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1){
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_received_message, parent, false)
            return ReceiveViewHolder(view)
        }
        else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sent_message, parent, false)
            return SentViewHolder(view)
        }
    }
    fun submitList(newList: List<Message>) {
        messList = newList
        notifyDataSetChanged()
    }
    fun submitUser(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
    override fun getItemCount(): Int {
        return messList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messList[position]
        Log.d("current message", currentMessage.toString())
        if (holder.javaClass == SentViewHolder::class.java){
            val viewHolder = holder as SentViewHolder
            viewHolder.tvMessage.text = currentMessage.content
            viewHolder.tvTime.text = Utils.formatTimestamp(currentMessage.time!!)
        }else{
            val viewHolder = holder as ReceiveViewHolder
            val sender = userList.firstOrNull { it.id == currentMessage.sender } as User
            Log.d("sender", sender.toString())
            viewHolder.tvMessage.text = currentMessage.content
            viewHolder.tvName.text = sender.name
            viewHolder.tvTime.text = Utils.formatTimestamp(currentMessage.time!!)
            if (sender.avatar != "")
                Glide.with(holder.itemView).load(sender.avatar).into(holder.imgAvatar)
        }
    }
}