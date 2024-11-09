package com.example.nush_hack_24

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val age: String = "",
    val bio: String = "",
    val role: String = "",
    val subjects: List<String> = listOf(),
    val rating: String = "",
    val pending: List<String> = listOf(),
    val connect: List<String> = listOf()
)

data class Message(
    val senderId: String = "",
    val receiverId: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class Review(val rating: Int, val reviewText: String)