package com.example.nush_hack_24

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.math.round

@Composable
fun TutorScreen(
    vm: MainViewModel = viewModel()
){
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(scrollState),

        ) {
        Text(
            "Logged in as: ${vm.userEmail} (TUTOR)",
            style = MaterialTheme.typography.headlineLarge
        )

        // State variables to hold the data
        var name by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }
        var bio by remember { mutableStateOf("") }
        var rev by remember { mutableStateOf("") }

        val subjects2: SnapshotStateList<String> = remember { mutableStateListOf() }

        var subject by remember { mutableStateOf("") }

        Log.d("die", vm.userUid)

        vm.db.collection("users").document(vm.userUid).get()
            .addOnSuccessListener { document ->
                name = document.get("name").toString()
                age = document.get("age").toString()
                bio = document.get("bio").toString()
                val num = document.get("rating")?.toString() ?: "0"
                val num2 = document.get("reviews")?.toString() ?: "0"
                val num3 = num.toFloat()
                val num4 = num2.toFloat()
                if (num4.toInt() == 0) rev = "No ratings yet."
                else rev = (round(num3*10/num4)/10).toString()
                val subjects = document.get("subjects") as? List<*>
                subjects2.clear()
                if (subjects != null) {
                    for (item in subjects){
                        subjects2.add(item.toString())
                    }
                }
                vm.userName = name
                vm.userAge = age
                vm.userBio = bio
                vm.selectedSubjects = subjects2
                subject = subjects?.joinToString(", ") ?: "No subjects available"
            }
            .addOnFailureListener { exception ->
                Log.w("DocumentFields", "Error getting document: ", exception)
            }

// Display Profile Data
        Spacer(modifier = Modifier.height(16.dp))
        Text("Name: $name")
        Text("Age: $age")
        Text("Bio: $bio")
        Text("Rating: $rev")
        Text("Subjects: $subject")


        Spacer(modifier = Modifier.height(24.dp))
        AnimatedVisibility(
            visible = !vm.isEditProfile
        ) {
            Button(
                onClick = { vm.isEditProfile = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }
        }
        AnimatedVisibility(
            visible = vm.isEditProfile
        ) {
            EditPage(subject = true)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = { vm.logoutUser { vm.isUserLoggedIn = false } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }
    }
}