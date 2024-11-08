package com.example.nush_hack_24

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

@Composable
fun ChatScreen(chatId: String, senderId: String, receiverName: String, onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Get the reference to the "users" collection and order by "timestamp" field
    db.collection("chats").document(chatId).collection("messages")
        .orderBy("timestamp", Query.Direction.ASCENDING) // or Query.Direction.DESCENDING
        .get()
        .addOnSuccessListener { result ->
            for (document in result) {
                // Access each document
                val user = document.toObject(User::class.java)
                Log.d("UserList", "User: ${user.toString()}")
            }
        }
        .addOnFailureListener { exception ->
            Log.w("Error", "Error getting documents: ", exception)
        }

    val messages = remember { mutableStateListOf<Message>() }

    LaunchedEffect(chatId) {
        ChatRepository.listenForMessages(chatId) { newMessage ->
            messages.add(newMessage)
        }
    }

    var text by remember { mutableStateOf("") }

    Column {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Chat with $receiverName",
                style = MaterialTheme.typography.h6,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(messages) { message ->
                MessageBubble(message, isSender = message.senderId == senderId)
            }
        }

        Row(modifier = Modifier.padding(8.dp)) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message...") }
            )
            IconButton(onClick = {
                val time = System.currentTimeMillis()
                if (text.isNotBlank()) {
                    ChatRepository.sendMessage(chatId, senderId, text, time)
                    text = ""
                }
            }) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}


@Composable
fun MessageBubble(message: Message, isSender: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    if (isSender) Color.Blue else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(8.dp)
        ) {
            Text(text = message.message, color = Color.White)
        }
    }
}