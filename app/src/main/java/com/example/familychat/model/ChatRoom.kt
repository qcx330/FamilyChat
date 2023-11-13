package com.example.familychat.model

data class ChatRoom(val roomId: String = "",
               val roomType: ChatRoomType = ChatRoomType.USER,
               val roomName: String? = "",
               val lastMessage: String? = "",
               val timestamp: Long? = 0,
                val message: Map<String, Message>? = null,
                val member: List<User> ? = null){

}
enum class ChatRoomType {
    USER,
    FAMILY
}
