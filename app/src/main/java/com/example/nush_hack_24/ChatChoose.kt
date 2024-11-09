package com.example.nush_hack_24

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChatChoose(
    vm: MainViewModel = viewModel()
) {
    val currentUserId = getCurrentUserId() ?: return
    Log.d("kys",currentUserId)
    // Fetch the user list when the screen loads
    LaunchedEffect(Unit) {
        vm.fetchUserList(currentUserId)
    }
    // Display the list of users in Logcat or update with UI
    vm.userList.forEach { user ->
        Log.d("UserListScreen", user.uid)
    }
    var selectedChatInfo by remember { mutableStateOf<Pair<String, String>?>(null) }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (selectedChatInfo == null) {
            // Show user list if no chat is open
            if (vm.userList.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(vm.userList) { user ->
                        ListItem(
                            text = { Text(user.email) },
                            modifier = Modifier.clickable {
                                // Directly set selectedChatId to open ChatScreen
                                Log.d("kys",user.uid)
                                selectedChatInfo = ChatRepository.getOrCreateChatId(currentUserId,user.uid) to user.uid
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

        // Logout button
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { vm.logoutUser { vm.isUserLoggedIn = false } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}

// Function to get the current user's ID
fun getCurrentUserId(): String? {
    val user = FirebaseAuth.getInstance().currentUser
    return user?.uid
}