package com.example.familychat.model

data class User(val name: String="",
            val email: String="",
            val avatar:String? = "",
            val id:String? = "",
            val familyId :String? ="",
            val token:String? = "") {
}