package com.example.familychat.model

data class Message(val id:String? = "",
    val sender: String?= "",
              val content:String?= "",
              val time: Long?= 0,
                val type : MessageType? = null) {
}
enum class MessageType {
    TEXT,
    IMAGE,
    LOCATION
}