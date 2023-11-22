package com.example.familychat.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.familychat.activity.ChatActivity
import com.google.firebase.messaging.RemoteMessage

class MyReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
//        if (intent?.action == FCMConstants.INTENT_ACTION_SHOW_NOTIFICATION) {
//            val messageTitle = intent.getStringExtra("messageTitle")
//            val messageContent = intent.getStringExtra("messageContent")
//            val chatRoomId = intent.getStringExtra("chatId")
//
//            // Launch the chat app with the specified chat room ID
//            val intent = Intent(context, ChatActivity::class.java)
//            intent.putExtra("chatId", chatRoomId)
//            context?.startActivity(intent)
//        }
    }
}