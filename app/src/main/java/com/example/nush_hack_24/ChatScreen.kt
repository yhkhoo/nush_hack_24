package com.example.nush_hack_24

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

@Composable
fun ChatScreen(chatId: String, senderId: String, receiverId: String, onBack: () -> Unit) {
    val db = FirebaseFirestore.getInstance()

    // Variable to hold the receiver's name
    var receiverName by remember { mutableStateOf("Loading...") }

    // Fetch the receiver's name from Firestore
    LaunchedEffect(receiverId) {
        db.collection("users").document(receiverId).get()
            .addOnSuccessListener { document ->
                // Get the "name" field from the document
                receiverName = document.getString("name") ?: "Unknown User"
            }
            .addOnFailureListener { exception ->
                Log.w("ChatScreen", "Error fetching user data: ", exception)
                receiverName = "Error Loading Name"
            }
    }

    // State for showing the tutor page or chat screen
    var showTutorPage by remember { mutableStateOf(false) }

    // List of messages from the chat
    val messages = remember { mutableStateListOf<Message>() }

    // Listen for incoming messages in the chat
    LaunchedEffect(chatId) {
        ChatRepository.listenForMessages(chatId) { newMessage ->
            messages.add(newMessage)
        }
    }

    var text by remember { mutableStateOf("") }

    if (showTutorPage) {
        // Show TutorPage when the state is true
        TutorPage(receiverId = receiverId)
    } else {
        // Show ChatScreen when the state is false
        Column {
            // Chat header with back button and the receiver's name
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
                    text = "Chat with $receiverName", // Display receiver's name here
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier
                        .clickable {
                            // Trigger the state change to show TutorPage when clicked
                            showTutorPage = true
                        }
                )
            }

            // Messages list
            LazyColumn() {
                items(messages) { message ->
                    MessageBubble(message, isSender = message.senderId == senderId)
                }
            }

            // Message input field and send button
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
