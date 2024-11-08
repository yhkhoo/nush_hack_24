package com.example.nush_hack_24

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    // Initialize Firebase Auth and Firestore instances
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }

    @Composable
    fun MyApp() {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var data by remember { mutableStateOf("") }
        var selectedRole by remember { mutableStateOf("Tutor") } // Default role
        var isUserLoggedIn by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf("") }
        var userRole by remember { mutableStateOf("") } // Store user role (Tutor or Tutee)
        var userEmail by remember { mutableStateOf("") } // Store user email
        var isSignUpPage by remember { mutableStateOf(false) } // Flag to toggle between Login and Sign Up page

        // Check if user is logged in on app start
        LaunchedEffect(auth.currentUser) {
            if (auth.currentUser != null) {
                // Fetch user email and role after user logs in
                userEmail = auth.currentUser?.email ?: "Unknown Email"
                fetchUserRole { role ->
                    userRole = role
                    isUserLoggedIn = true
                }
            }
        }

        // Main UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isUserLoggedIn) {
                // If user is logged in, show data entry page
                Text("Logged in as: $userEmail ($userRole)")
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { logoutUser { isUserLoggedIn = false } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(statusMessage)
            } else {
                // Show Login or Sign-Up page if the user is not logged in
                if (isSignUpPage) {
                    // Show Sign-Up page
                    Text("Sign Up")

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role selection for Tutor or Tutee
                    Text("Select Role:")
                    Row(modifier = Modifier.selectableGroup()) {
                        // Tutor Option
                        RadioButton(
                            selected = selectedRole == "Tutor",
                            onClick = { selectedRole = "Tutor" }
                        )
                        Text("Tutor", modifier = Modifier.padding(start = 8.dp))

                        Spacer(modifier = Modifier.width(16.dp))

                        // Tutee Option
                        RadioButton(
                            selected = selectedRole == "Tutee",
                            onClick = { selectedRole = "Tutee" }
                        )
                        Text("Tutee", modifier = Modifier.padding(start = 8.dp))
                    }

                    Button(
                        onClick = { registerUser(email, password, selectedRole) { success ->
                            if (success) {
                                isUserLoggedIn = true
                                // Fetch the user's email and role from Firestore after login
                                userEmail = email
                                fetchUserRole { role ->
                                    userRole = role
                                }
                            }
                            statusMessage = if (success) "Registration successful!" else "Registration failed"
                        }},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign Up")
                    }

                    TextButton(
                        onClick = { isSignUpPage = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Already have an account? Log In")
                    }
                } else {
                    // Show Login page
                    Text("Login")

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { loginUser(email, password) { success ->
                            if (success) {
                                isUserLoggedIn = true
                                // Fetch the user's email and role from Firestore after login
                                userEmail = email
                                fetchUserRole { role ->
                                    userRole = role
                                }
                            }
                            statusMessage = if (success) "Login successful!" else "Login failed"
                        }},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Login")
                    }

                    TextButton(
                        onClick = { isSignUpPage = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Don't have an account? Sign Up")
                    }
                }
            }
        }
    }

    private fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                callback(task.isSuccessful)
            }
    }

    private fun registerUser(email: String, password: String, role: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: return@addOnCompleteListener

                    val userData = hashMapOf(
                        "email" to email,
                        "role" to role // Save the role as part of the user's data
                    )

                    db.collection("users").document(userId)
                        .set(userData)
                        .addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            callback(false)
                        }
                } else {
                    callback(false)
                }
            }
    }

    private fun fetchUserRole(callback: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role") ?: "Unknown"
                    callback(role)
                }
                .addOnFailureListener {
                    callback("Error fetching role")
                }
        } else {
            callback("No user logged in")
        }
    }

    // Logout Functionality
    private fun logoutUser(callback: () -> Unit) {
        auth.signOut()
        callback() // Reset state for logout
    }
}
