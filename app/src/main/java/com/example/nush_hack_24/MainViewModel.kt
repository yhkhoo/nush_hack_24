package com.example.nush_hack_24

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.round

class MainViewModel : ViewModel() {
    val subjects = listOf<String>("English", "Chinese", "Tamil", "Hindi", "Biology", "Chemistry", "Physics", "Math", "Geography", "Literature", "Social Studies", "History")

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
    var userUid by mutableStateOf("") // New UID variable
    var userRating by mutableStateOf("")

    // flags
    var isSignUpPage by mutableStateOf(false) // Flag to toggle between Login and Sign Up page
    var isEditProfile by mutableStateOf(false)
    var isSearch by mutableStateOf(false)

    // Profile details
    var userName by mutableStateOf("")
    var userBio by mutableStateOf("")
    var userAge by mutableStateOf("")
    var selectedSubjects = mutableStateListOf<String>() // Subjects the user is enrolled in
    var foundTutors = mutableStateListOf<User>()

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
                val user = FirebaseAuth.getInstance().currentUser
                userUid = user?.uid ?: "" // Set UID
                callback(task.isSuccessful)
            }
    }

    // Function to register the user
    fun registerUser(email: String, password: String, role: String, birth: String, userName: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userId = user?.uid ?: return@addOnCompleteListener

                    val userData = hashMapOf(
                        "email" to email,
                        "role" to role, // Save the role as part of the user's data
                        "age" to birth,
                        "name" to userName
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
                    userAge = document.getString("age") ?: ""
                    selectedSubjects = (document.get("subjects") as? SnapshotStateList<String>) ?: mutableStateListOf<String>()
                }
                .addOnFailureListener {
                    statusMessage = "Error fetching profile"
                }
        }
    }

    // Update the profile in Firestore
    fun updateProfile(name: String, bio: String, age: String, subjects: List<String>, callback: (Boolean) -> Unit) {
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
    val userList: SnapshotStateList<User> = mutableStateListOf()

    // Function to fetch all users from Firestore and populate the userList
    fun fetchUserList(currentUserId: String) {
        //Log.w("A","A")
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                userList.clear()
                for (user in result){
                    Log.d("UserList", "User: ${user.id}")



                    db.collection("users")
                        .document(user.id)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                // Access multiple fields
                                val email = document.getString("email") ?: "Unknown"
                                val age = document.getString("age") ?: "0"
                                val name = document.getString("name") ?: "Unknown"
                                val role = document.getString("role") ?: "Unknown"
                                val bio = document.getString("bio") ?: "Unknown"
                                val subjects = (document.get("subjects") as? List<String>) ?: listOf()
                                val pending = (document.get("pending") as? List<String>) ?: listOf()
                                val connect = (document.get("connect") as? List<String>) ?: listOf()
                                val num = document.get("rating")?.toString() ?: "0"
                                val num2 = document.get("reviews")?.toString() ?: "0"
                                val rev = getRating(num, num2)
                                Log.wtf("jover", subjects.toString())

                                val user2  = User(uid = user.id, email = email, age=age, name=name, role=role, bio=bio, subjects=subjects, rating=rev, pending=pending, connect=connect)
                                onUserFetched(user2)
                            } else {
                                Log.w("fetchUserDetails", "No such document")
                            }
                        }

                }
            }
            .addOnFailureListener { exception ->
                Log.e("MainViewModel", "Error fetching users: ", exception)
            }
    }

    fun getRating(num:String, num2:String): String{
        val num3 = num.toFloat()
        val num4 = num2.toFloat()
        if (num4.toInt() == 0) return "No ratings yet."
        else return (round(num3*10/num4) /10).toString()
    }

    fun onUserFetched(user2: User){
        userList.add(user2)
    }

    fun searchTutors(subject: String){
        foundTutors.clear()
        fetchUserList(auth.currentUser!!.uid)
        userList.forEach { user ->
            if(user.role == "Tutor" && subject in user.subjects){
                foundTutors.add(user)
            }
        }
    }
}
