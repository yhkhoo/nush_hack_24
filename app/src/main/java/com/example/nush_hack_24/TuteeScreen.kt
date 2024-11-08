package com.example.nush_hack_24

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TuteeScreen(
    vm: MainViewModel = viewModel()
){
    // If user is logged in, show data entry page
    Text("Logged in as: ${vm.userEmail} (TUTEE)")
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = { vm.logoutUser { vm.isUserLoggedIn = false } },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Logout")
    }
    Spacer(modifier = Modifier.height(16.dp))
    Text(vm.statusMessage)
}