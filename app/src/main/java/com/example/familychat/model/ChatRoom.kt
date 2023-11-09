package com.example.familychat.model

class ChatRoom(val roomId: String = "",
               val roomName: String? = "",
               val lastMessage: String? = "",
               val timestamp: Long? = 0,
                val message: Map<String, Message>? = null,
                val members: List<String> ? = null){
}