package com.example.nush_hack_24

data class User(
    val uid: String = "",
    val email: String = ""
)

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)