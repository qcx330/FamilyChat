package com.example.familychat.model

data class Message(val sender: String?= "",
              val content:String?= "",
              val time: Long?= 0) {
}