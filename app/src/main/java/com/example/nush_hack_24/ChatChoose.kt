package com.example.nush_hack_24

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatChoose(
    vm: MainViewModel = viewModel()
) {
    val currentUserId = getCurrentUserId() ?: return

    // State to hold the list of connected users
    var connectedUsers by remember { mutableStateOf<List<User>>(emptyList()) }
    // State to track the selected chat details
    var selectedChatInfo by remember { mutableStateOf<Pair<String, String>?>(null) }

    // Fetch the user list when the screen loads
    LaunchedEffect(currentUserId) {
        vm.fetchUserList(currentUserId)

        // Fetch the current user's "connect" list from Firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(currentUserId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get the "connect" list
                    val connectList = (document.get("connect") as? List<String>) ?: listOf()

                    // Fetch user data for each user ID in the connect list
                    connectList.forEach { userId ->
                        db.collection("users").document(userId).get()
                            .addOnSuccessListener { userDoc ->
                                if (userDoc.exists()) {
                                    val email = userDoc.getString("email") ?: "Unknown"
                                    val name = userDoc.getString("name") ?: "Unknown"
                                    val user = User(uid = userId, email = email, name = name)

                                    // Add the user to the connected users list
                                    connectedUsers = connectedUsers + user
                                }
                            }
                    }
                } else {
                    Log.w("fetchUserDetails", "No such document")
                }
            }
    }

    // Display the list of connected users
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (selectedChatInfo == null) {
            // Show user list if no chat is open
            if (connectedUsers.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(connectedUsers) { user ->
                        ListItem(
                            text = { Text(user.name) },
                            modifier = Modifier.clickable {
                                // Directly set selectedChatId to open ChatScreen
                                selectedChatInfo = ChatRepository.getOrCreateChatId(
                                    currentUserId,
                                    user.uid
                                ) to user.uid
                            }
                        )
                    }
                }
            }
        } else {
            // Show ChatScreen when a chat is selected
            ChatScreen(
                chatId = selectedChatInfo!!.first,
                senderId = currentUserId,
                receiverId = selectedChatInfo!!.second
            ) { selectedChatInfo = null }
        }
    }
}

// Function to get the current user's ID
fun getCurrentUserId(): String? {
    val user = FirebaseAuth.getInstance().currentUser
    return user?.uid
}