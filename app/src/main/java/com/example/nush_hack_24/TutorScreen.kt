package com.example.nush_hack_24

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TutorScreen(
    vm: MainViewModel = viewModel()
){
    androidx.compose.material3.Text(
        "Logged in as: ${vm.userEmail} (TUTOR)",
        style = MaterialTheme.typography.headlineLarge
    )

    Spacer(modifier = Modifier.height(16.dp))

    // Display Profile Data
    androidx.compose.material3.Text("Name: ${vm.userName}")
    androidx.compose.material3.Text("Age: ${vm.userAge}")
    androidx.compose.material3.Text("Bio: ${vm.userBio}")
    androidx.compose.material3.Text("Subjects: ${vm.selectedSubjects.joinToString(", ")}")

    Spacer(modifier = Modifier.height(24.dp))
    AnimatedVisibility(
        visible = !vm.isEditProfile
    ) {
        androidx.compose.material3.Button(
            onClick = { vm.isEditProfile = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            androidx.compose.material3.Text("Edit Profile")
        }
    }
    AnimatedVisibility(
        visible = vm.isEditProfile
    ) {
        EditPage(subject = true)
    }
    Spacer(modifier = Modifier.height(16.dp))

    // Logout Button
    androidx.compose.material3.Button(
        onClick = { vm.logoutUser { vm.isUserLoggedIn = false } },
        modifier = Modifier.fillMaxWidth()
    ) {
        androidx.compose.material3.Text("Logout")
    }
}