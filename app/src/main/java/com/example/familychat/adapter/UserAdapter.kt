package com.example.familychat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.familychat.R
import com.example.familychat.model.User
import com.example.familychat.fragment.ProfileFragment
import com.google.firebase.auth.FirebaseAuth

class UserAdapter(val onItemClick: RvInterface) : RecyclerView.Adapter<ViewHolder>() {
    private var userList: List<User> = emptyList()
    private val current = 1
    private val others = 0
    val currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
    inner class CurrentViewHolder(itemView: View) :ViewHolder(itemView){
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgAvatar = itemView.findViewById<ImageView>(R.id.imgAvatar)
        val btnView = itemView.findViewById<Button>(R.id.btnView)
        fun bind (user: User){
            tvName.text = user.name
            if (user.avatar != "")
                Glide.with(itemView).load(user.avatar).into(imgAvatar)
        }
    }
    inner class OthersViewHolder(itemView: View) :ViewHolder(itemView){
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgAvatar = itemView.findViewById<ImageView>(R.id.imgAvatar)
        val btnView = itemView.findViewById<Button>(R.id.btnView)
        val btnMessage = itemView.findViewById<Button>(R.id.btnMessage)
        fun bind (user: User){
            tvName.text = user.name
            if (user.avatar != "")
                Glide.with(itemView).load(user.avatar).into(imgAvatar)
        }

    }
    override fun getItemViewType(position: Int): Int {
        val user = userList[position]
        return if (currentUserId.equals(user.id))
            current
        else others
    }
    fun submitList(newList: List<User>) {
        userList = newList
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 1) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_current_user, parent, false)
            return CurrentViewHolder(view)
        }else {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.row_member, parent, false)
            return OthersViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        if (holder.javaClass == CurrentViewHolder::class.java){
            val viewHolder = holder as CurrentViewHolder
            viewHolder.bind(user)
            viewHolder.btnView.setOnClickListener(){
                val transaction = (holder.itemView.context as AppCompatActivity).supportFragmentManager.beginTransaction()
                transaction.setCustomAnimations(R.anim.slide_out_right,R.anim.slide_in_left)
                transaction.replace(R.id.frameLayout, ProfileFragment())
                transaction.commit()
            }
        }else{
            val viewHolder = holder as OthersViewHolder
            viewHolder.bind(user)
            viewHolder.btnView.setOnClickListener(){

            }
            viewHolder.btnMessage.setOnClickListener(){
                onItemClick.OnClickItem(position)
            }
        }
    }
}

