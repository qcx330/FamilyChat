package com.example.familychat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.R
import com.example.familychat.model.User
import com.squareup.picasso.Picasso

class UserAdapter() : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
    private var userList: List<User> = emptyList()
    inner class UserViewHolder(itemView: View) :RecyclerView.ViewHolder(itemView){
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgAvatar = itemView.findViewById<ImageView>(R.id.imgAvatar)
        fun bind (user: User){
            tvName.text = user.name
            if (user.avatar != "")
                Picasso.get().load(user.avatar).into(imgAvatar)
        }

    }
    fun submitList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_member, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }
}

