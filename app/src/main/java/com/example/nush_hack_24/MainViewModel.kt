package com.example.nush_hack_24

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel() : ViewModel() {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var birthyear by mutableStateOf("")
    var selectedRole by mutableStateOf("Tutor") // Default role
    var isUserLoggedIn by mutableStateOf(false)
    var statusMessage by mutableStateOf("")
    var userRole by mutableStateOf("") // Store user role (Tutor or Tutee)
    var userEmail by mutableStateOf("") // Store user email
    var isSignUpPage by mutableStateOf(false) // Flag to toggle between Login and Sign Up page

    init {
        if (auth.currentUser != null) {
            // Fetch user email and role after user logs in
            userEmail = auth.currentUser?.email ?: "Unknown Email"
            fetchUserRole { role ->
                userRole = role
                isUserLoggedIn = true
            }
        }
    }

    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    fun registerUser(email: String, password: String, role: String, birth: String, callback: (Boolean) -> Unit) {
        val birthyear = birth.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: return@addOnCompleteListener

                    val userData = hashMapOf(
                        "email" to email,
                        "role" to role, // Save the role as part of the user's data
                        "birthyear" to birthyear,
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

    fun fetchUserRole(callback: (String) -> Unit) {
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
    fun logoutUser(callback: () -> Unit) {
        auth.signOut()
        callback() // Reset state for logout
    }
}