package com.example.nush_hack_24

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

object ChatRepository {

    private val db = FirebaseFirestore.getInstance()

    // Get or create a chat ID for two users
    fun getOrCreateChatId(senderId: String, receiverId: String): String {
        val chatId = if (senderId < receiverId) senderId +'_'+ receiverId else receiverId +'_'+ senderId
        Log.d("A",chatId)
        val chatDocRef: DocumentReference = db.collection("chats").document(chatId)

        chatDocRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (!document.exists()) {
                    // If chat doesn't exist, create it
                    chatDocRef.set(hashMapOf("users" to listOf(senderId, receiverId)))
                }
            }
        }
        return chatId
    }

    // Send a message to Firestore under the specified chat ID
    fun sendMessage(chatId: String, senderId: String, messageText: String, time: Long) {
        val message = Message(senderId = senderId, message = messageText, timestamp = time)
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
    }

    // Listen for new messages in real-time in a specific chat
    fun listenForMessages(chatId: String, onMessageReceived: (Message) -> Unit) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == DocumentChange.Type.ADDED) {
                        val message = change.document.toObject(Message::class.java)
                        onMessageReceived(message)
                    }
                }
            }
    }
}