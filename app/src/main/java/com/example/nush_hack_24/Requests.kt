package com.example.nush_hack_24

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

@SuppressLint("UnrememberedMutableState")
@Composable
fun Requests() {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val pendingUsers = remember { mutableStateListOf<User>() }
    val isDataLoaded = remember { mutableStateOf(false) } // To track loading state
    val selectedUser = remember { mutableStateOf<User?>(null) } // To store selected user for dialog

    // Fetch pending users data
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Fetch the 'pending' list from the document
                        val pendingList = (document.get("pending") as? List<String>) ?: listOf()

                        // Now, fetch the details of all users in the pending list
                        val fetchUserTasks = mutableListOf<Task<DocumentSnapshot>>()

                        pendingList.forEach { id ->
                            val task = db.collection("users")
                                .document(id)
                                .get()
                                .addOnSuccessListener { userDoc ->
                                    if (userDoc.exists()) {
                                        val name = userDoc.getString("name") ?: "Unknown"
                                        val email = userDoc.getString("email") ?: "Unknown"
                                        val age = userDoc.getString("age") ?: "0"
                                        val bio = userDoc.getString("bio") ?: "No bio available"
                                        val user = User(uid = id, name = name, email = email, age = age, bio = bio)
                                        pendingUsers.add(user) // Add the user to the list
                                    } else {
                                        Log.w("PendingUsers", "No such document for userId: $id")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("PendingUsers", "Error fetching user data", exception)
                                }

                            fetchUserTasks.add(task)
                        }

                        // Wait for all user data fetch tasks to complete
                        Tasks.whenAllComplete(fetchUserTasks).addOnCompleteListener {
                            isDataLoaded.value = true // Set loading to false after fetching is done
                        }
                    } else {
                        Log.w("PendingUsers", "No such document for current user.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("PendingUsers", "Error fetching current user document", exception)
                }
        }
    }

    // Display the list of users once data is loaded
    if (isDataLoaded.value) {
        Column {
            pendingUsers.forEach { user ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .clickable {
                            selectedUser.value = user // Set selected user when card is clicked
                        }
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(user.name, style = MaterialTheme.typography.subtitle1)
                    }
                }
            }
        }
    }

    // If a user is selected, show the dialog
    selectedUser.value?.let { user ->
        AcceptRejectDialog(
            user = user,
            onAccept = { selectedUserId ->
                // Add user to 'connect' field and remove from 'pending' field
                handleAcceptRejectAction(selectedUserId, action = "accept")
            },
            onReject = { selectedUserId ->
                // Remove user from 'pending' field
                handleAcceptRejectAction(selectedUserId, action = "reject")
            },
            onDismiss = { selectedUser.value = null } // Dismiss dialog
        )
    }
}

@Composable
fun AcceptRejectDialog(
    user: User,
    onAccept: (String) -> Unit,
    onReject: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Pending Request from ${user.name}") },
        text = {
            Column {
                Text("Do you want to accept or reject this request?")
                Spacer(modifier = Modifier.height(16.dp))
                Text("Name: ${user.name}")
                Text("Bio: ${user.bio}")
                Text("Email: ${user.email}")
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAccept(user.uid) // Handle accept action
                    onDismiss()
                }
            ) {
                Text("Accept")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onReject(user.uid) // Handle reject action
                    onDismiss()
                }
            ) {
                Text("Reject")
            }
        }
    )
}

fun handleAcceptRejectAction(userId: String, action: String) {
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser?.uid ?: return

    // Transaction to safely update the lists
    db.runTransaction { transaction ->
        val userDoc = db.collection("users").document(currentUser)

        when (action) {
            "accept" -> {
                currentUser?.let { Id ->
                    val userDocRef = db.collection("users").document(userId)
                    val tutorDocRef = db.collection("users").document(Id)

                    db.runBatch { batch ->
                        // Add tutorId to the user's "pending" list
                        batch.update(userDocRef, "connect", FieldValue.arrayUnion(Id))

                        // Add userId to the tutor's "pending" list
                        batch.update(tutorDocRef, "connect", FieldValue.arrayUnion(userId))
                    }
                }
                currentUser?.let { Id ->
                    val userDocRef = db.collection("users").document(userId)
                    val tutorDocRef = db.collection("users").document(Id)

                    db.runBatch { batch ->
                        // Add tutorId to the user's "pending" list
                        batch.update(userDocRef, "pending", FieldValue.arrayRemove(Id))

                        // Add userId to the tutor's "pending" list
                        batch.update(tutorDocRef, "pending", FieldValue.arrayRemove(userId))
                    }
                }
            }
            "reject" -> {
                currentUser?.let { Id ->
                    val userDocRef = db.collection("users").document(userId)
                    val tutorDocRef = db.collection("users").document(Id)

                    db.runBatch { batch ->
                        // Add tutorId to the user's "pending" list
                        batch.update(userDocRef, "pending", FieldValue.arrayRemove(Id))

                        // Add userId to the tutor's "pending" list
                        batch.update(tutorDocRef, "pending", FieldValue.arrayRemove(userId))
                    }
                }
            }
        }

        // Update the tutor's pending list (same action: accept or reject)
        val tutorDoc = db.collection("users").document(userId)
    }
}