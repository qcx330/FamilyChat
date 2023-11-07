package com.example.familychat.adapter

import android.content.IntentSender.SendIntentException
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.familychat.R
import com.example.familychat.model.Message
import com.google.firebase.auth.FirebaseAuth
import com.makeramen.roundedimageview.RoundedImageView

class MessageAdapter: RecyclerView.Adapter<ViewHolder>() {
    private var messList: List<Message> = emptyList()

    val received_item = 1
    val sent_item = 1

    inner class SentViewHolder(itemView:View):ViewHolder(itemView){
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
    }
    inner class ReceiveViewHolder(itemView:View):ViewHolder(itemView){
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgAvatar = itemView.findViewById<RoundedImageView>(R.id.imgAvatar)
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
    override fun getItemCount(): Int {
        return messList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messList[position]
        if (holder.javaClass == SentViewHolder::class.java){
            val viewHolder = holder as SentViewHolder
            viewHolder.tvMessage.text = currentMessage.content
        }else{
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.tvMessage.text == currentMessage.content
            viewHolder.tvName.text == currentMessage.sender
//            viewHolder.imgAvatar.setImageURI(currentMessage.sender.avatar!!.toUri())
        }
    }
}