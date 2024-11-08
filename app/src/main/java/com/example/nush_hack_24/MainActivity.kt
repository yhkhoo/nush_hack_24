package com.example.nush_hack_24

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
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
        var isUserLoggedIn by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!isUserLoggedIn) {
                Text("Login or Sign Up")

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
                        isUserLoggedIn = success
                        statusMessage = if (success) "Login successful!" else "Login failed"
                    }},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Login")
                }

                Button(
                    onClick = { registerUser(email, password) { success ->
                        isUserLoggedIn = success
                        statusMessage = if (success) "Registration successful!" else "Registration failed"
                    }},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sign Up")
                }
            } else {
                Text("Enter data to save:")

                OutlinedTextField(
                    value = data,
                    onValueChange = { data = it },
                    label = { Text("Data") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { saveData(data) { message ->
                        statusMessage = message
                    }},
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Data")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(statusMessage)
        }
    }

    private fun loginUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                callback(task.isSuccessful)
            }
    }

    private fun registerUser(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                callback(task.isSuccessful)
            }
    }

    private fun saveData(data: String, callback: (String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userId = user.uid
            val dataMap = hashMapOf("userData" to data)

            db.collection("users").document(userId)
                .set(dataMap)
                .addOnSuccessListener {
                    callback("Data saved successfully")
                }
                .addOnFailureListener { e ->
                    callback("Error saving data: ${e.message}")
                }
        } else {
            callback("User not logged in")
        }
    }
}
