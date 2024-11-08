package com.example.nush_hack_24

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainViewModel : ViewModel() {
    val subjects = listOf("English", "Chinese", "Tamil", "Hindi", "Biology", "Chemistry", "Physics", "Math", "Geography", "Literature", "Social Studies", "History")

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // User authentication and profile details
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var birthyear by mutableStateOf("")
    var selectedRole by mutableStateOf("Tutor") // Default role
    var isUserLoggedIn by mutableStateOf(false)
    var statusMessage by mutableStateOf("")
    var userRole by mutableStateOf("") // Store user role (Tutor or Tutee)
    var userEmail by mutableStateOf("") // Store user email

    // flags
    var isSignUpPage by mutableStateOf(false) // Flag to toggle between Login and Sign Up page
    var isEditProfile by mutableStateOf(false)

    // Profile details
    var userName by mutableStateOf("")
    var userBio by mutableStateOf("")
    var userAge by mutableStateOf(0)
    var selectedSubjects = mutableListOf<String>() // Subjects the user is enrolled in

    init {
        if (auth.currentUser != null) {
            // Fetch user email and role after user logs in
            userEmail = auth.currentUser?.email ?: "Unknown Email"
            fetchUserRole { role ->
                userRole = role
                isUserLoggedIn = true
                fetchUserProfile()
            }
        }
    }

    // Function to login the user
    fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    // Function to register the user
    fun registerUser(email: String, password: String, role: String, birth: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: return@addOnCompleteListener

                    val userData = hashMapOf(
                        "email" to email,
                        "role" to role, // Save the role as part of the user's data
                        "age" to birth,
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

    // Fetch user role
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

    // Fetch user profile details (name, bio, age, subjects)
    fun fetchUserProfile() {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    userName = document.getString("name") ?: "Unknown"
                    userBio = document.getString("bio") ?: "No bio"
                    userAge = document.getString("age")?.toInt() ?: 0
                    selectedSubjects = (document.get("subjects") as? List<String>)?.toMutableList() ?: mutableListOf()
                }
                .addOnFailureListener {
                    statusMessage = "Error fetching profile"
                }
        }
    }

    // Update the profile in Firestore
    fun updateProfile(name: String, bio: String, age: Int, subjects: List<String>, callback: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val updatedData = hashMapOf(
                "name" to name,
                "bio" to bio,
                "age" to age,
                "subjects" to subjects
            )

            db.collection("users").document(userId)
                .update(updatedData)
                .addOnSuccessListener {
                    userName = name
                    userBio = bio
                    userAge = age
                    selectedSubjects = subjects.toMutableList()
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        }
    }

    // Logout Functionality
    fun logoutUser(callback: () -> Unit) {
        auth.signOut()
        callback() // Reset state for logout
    }
}
