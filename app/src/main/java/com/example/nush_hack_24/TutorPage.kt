package com.example.nush_hack_24

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun TutorPage(
    receiverId: String,
    vm: MainViewModel = viewModel()
) {

    // State variables to hold the data
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }

    Log.d("kkk", receiverId)
    vm.db.collection("users").document(receiverId).get()
        .addOnSuccessListener { document ->
            name = document.get("name").toString()
            age = document.get("age").toString()
            bio = document.get("bio").toString()

            val subjects = document.get("subjects") as? List<*>
            subject = subjects?.joinToString(", ") ?: "No subjects available"

            Log.d("kk", subject)
        }
        .addOnFailureListener { exception ->
            Log.w("DocumentFields", "Error getting document: ", exception)
        }

// Display Profile Data
    Spacer(modifier = Modifier.height(16.dp))
    Text("Age: $age")
    Text("Bio: $bio")
    Text("Subjects: $subject")


    Spacer(modifier = Modifier.height(24.dp))

    // Leave a review button
    LeaveReviewButton(tutorId = receiverId)

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun LeaveReviewButton(tutorId: String) {
    var showDialog by remember { mutableStateOf(false) }

    // Button to show review dialog
    Button(
        onClick = { showDialog = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Leave a review")
    }

    // Show dialog when button is clicked
    if (showDialog) {
        ReviewDialog(
            onDismiss = { showDialog = false },
            onSubmit = { rating, reviewText ->
                saveReviewToFirebase(tutorId, rating, reviewText)
                showDialog = false // Close dialog after submitting
            }
        )
    }
}

@Composable
fun ReviewDialog(onDismiss: () -> Unit, onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Leave a Review") },
        text = {
            Column {
                // Rating section (1-5 stars)
                Row {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Text(
                                text = if (star <= rating) "★" else "☆",
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Text review section
                BasicTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .background(Color.Gray.copy(alpha = 0.1f)),
                    textStyle = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (rating > 0 && reviewText.text.isNotBlank()) {
                    onSubmit(rating, reviewText.text) // Save the review
                } else {
                    // Show an error message or handle validation
                    Log.d("ReviewDialog", "Please provide both a rating and a text review.")
                }
            }) {
                Text("Submit")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

fun saveReviewToFirebase(tutorId: String, rating: Int, reviewText: String) {
    val db = FirebaseFirestore.getInstance()

    // Create a new review document
    val reviewData = hashMapOf(
        "rating" to rating,
        "reviewText" to reviewText,
        "timestamp" to System.currentTimeMillis()
    )

    // Save the review to the tutor's document
    db.collection("users").document(tutorId).collection("reviews")
        .add(reviewData)
        .addOnSuccessListener {
            Log.d("Review", "Review saved successfully!")
            // Fetch the current totalRating and totalReviews
            db.collection("users").document(tutorId).get()
                .addOnSuccessListener { document ->
                    val currentTotalRating = document.getLong("rating") ?: 0L
                    val currentTotalReviews = document.getLong("reviews") ?: 0L

                    // Calculate new totals
                    val newTotalRating = currentTotalRating + rating
                    val newTotalReviews = currentTotalReviews + 1

                    // Update the user document with new totals
                    db.collection("users").document(tutorId)
                        .update(
                            mapOf(
                                "rating" to newTotalRating,
                                "reviews" to newTotalReviews
                            )
                        )
                        .addOnSuccessListener {
                            Log.d("Review", "User totals updated successfully!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Review", "Error updating user totals", e)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w("Review", "Error fetching user totals", e)
                }
        }
        .addOnFailureListener { e ->
            Log.w("Review", "Error saving review", e)
        }
}