package com.example.familychat.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.example.familychat.R
import com.example.familychat.Utils
import com.example.familychat.model.Message
import com.example.familychat.model.MessageType
import com.example.familychat.model.User
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.makeramen.roundedimageview.RoundedImageView

class MessageAdapter : RecyclerView.Adapter<ViewHolder>()  {
    private var messList: List<Message> = emptyList()
    private val userRef = FirebaseDatabase.getInstance().getReference("User")

    private val text_received_item = 0
    private val image_received_item = 1
    private val text_sent_item = 2
    private val image_sent_item = 3
    private val location_received_item = 4
    private val location_sent_item = 5

    inner class SentViewHolder(itemView: View) : ViewHolder(itemView) {
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val imgMess = itemView.findViewById<ImageView>(R.id.imgMessage)
        val map = itemView.findViewById<MapView>(R.id.map)
    }

    inner class ReceiveViewHolder(itemView: View) : ViewHolder(itemView) {
        val tvMessage = itemView.findViewById<TextView>(R.id.tvMessage)
        val tvName = itemView.findViewById<TextView>(R.id.tvName)
        val imgAvatar = itemView.findViewById<RoundedImageView>(R.id.imgAvatar)
        val tvTime = itemView.findViewById<TextView>(R.id.tvTime)
        val imgMess = itemView.findViewById<ImageView>(R.id.imgMessage)
        val map = itemView.findViewById<MapView>(R.id.map)
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messList[position]
        return when {
            currentMessage.sender == FirebaseAuth.getInstance().currentUser?.uid -> {
                if (currentMessage.type == MessageType.TEXT)
                    return text_sent_item
                else if (currentMessage.type == MessageType.IMAGE)
                    return image_sent_item
                else return location_sent_item

            }

            else -> {
                if (currentMessage.type == MessageType.TEXT)
                    return text_received_item
                else if (currentMessage.type == MessageType.IMAGE)
                    return image_received_item
                else return location_received_item
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_received_message, parent, false)
            return ReceiveViewHolder(view)
        } else if (viewType == 1) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_received_image, parent, false)
            return ReceiveViewHolder(view)
        } else if (viewType == 2) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sent_message, parent, false)
            return SentViewHolder(view)
        } else if (viewType == 3) {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_sent_image, parent, false)
            return SentViewHolder(view)
        } else if (viewType == 4) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_received_location, parent, false)
            return ReceiveViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_sent_location, parent, false)
            return ReceiveViewHolder(view)
        }
    }

    fun submitList(newList: List<Message>) {
        messList = newList
    }

    override fun getItemCount(): Int {
        return messList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentMessage = messList[position]
        Log.d("current message", currentMessage.toString())
        if (holder.javaClass == SentViewHolder::class.java) {
            val viewHolder = holder as SentViewHolder
            if (currentMessage.type == MessageType.TEXT)
                viewHolder.tvMessage.text = currentMessage.content
            else if (currentMessage.type == MessageType.IMAGE)
                Glide.with(holder.itemView).load(currentMessage.content).into(holder.imgMess)
            else {
                if (viewHolder.map != null) {
                    // Initialise the MapView
                    viewHolder.map.onCreate(null)
                    viewHolder.map.onResume()
                    // Set the map ready callback to receive the GoogleMap object
//                    viewHolder.map.getMapAsync { googleMap ->
//                        googleMap.moveCamera(
//                            CameraUpdateFactory.newLatLngZoom(
//                                LatLng(
//                                    chat.getLat(),
//                                    chat.getLon()
//                                ), 13.0F
//                            )
//                        )
//                        googleMap.addMarker(
//                            MarkerOptions().position(
//                                LatLng(
//                                    chat.getLat(),
//                                    chat.getLon()
//                                )
//                            )
//                        )
//                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
//                        googleMap.getUiSettings().setAllGesturesEnabled(false)
//                    }
                }
            }
            viewHolder.tvTime.text = Utils.formatTimestamp(currentMessage.time!!)
        } else {
            val viewHolder = holder as ReceiveViewHolder
            userRef.child(currentMessage.sender!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val user = dataSnapshot.getValue(User::class.java)!!
                        user.let {
                            if (currentMessage.type == MessageType.TEXT)
                                viewHolder.tvMessage.text = currentMessage.content
                            else if (currentMessage.type == MessageType.IMAGE)
                                Glide.with(holder.itemView).load(currentMessage.content)
                                    .into(holder.imgMess)
                            else {
                                if (viewHolder.map != null) {
                                    // Initialise the MapView
                                    viewHolder.map.onCreate(null);
                                    viewHolder.map.onResume();
//                                    viewHolder.map.getMapAsync { googleMap ->
//                                        googleMap.moveCamera(
//                                            CameraUpdateFactory.newLatLngZoom(
//                                                LatLng(
//                                                    chat.getLat(),
//                                                    chat.getLon()
//                                                ), 13.0F
//                                            )
//                                        )
//                                        googleMap.addMarker(
//                                            MarkerOptions().position(
//                                                LatLng(
//                                                    chat.getLat(),
//                                                    chat.getLon()
//                                                )
//                                            )
//                                        )
//                                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)
//                                        googleMap.getUiSettings().setAllGesturesEnabled(false)
//                                    }
                                }
                            }
                            viewHolder.tvName.text = user.name
                            viewHolder.tvTime.text = Utils.formatTimestamp(currentMessage.time!!)
                            if (user.avatar != "")
                                Glide.with(holder.itemView).load(user.avatar).into(holder.imgAvatar)
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("get current user", databaseError.message)
                    }
                })

        }
    }
    fun updateList(newList: List<Message>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int = messList.size
            override fun getNewListSize(): Int = newList.size
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                diffCallback.areItemsTheSame(messList[oldItemPosition], newList[newItemPosition])

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                diffCallback.areContentsTheSame(messList[oldItemPosition], newList[newItemPosition])
        })

        messList= newList
        diffResult.dispatchUpdatesTo(this)
    }
    private val diffCallback = object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
}