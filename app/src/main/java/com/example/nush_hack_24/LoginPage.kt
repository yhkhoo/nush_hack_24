package com.example.nush_hack_24

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginPage(vm: MainViewModel = viewModel()){
    // Show Login or Sign-Up page if the user is not logged in
    if (vm.isSignUpPage) {
        // Show Sign-Up page
        Text("Sign Up")

        TextField(
            value = vm.email,
            onValueChange = { vm.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = vm.password,
            onValueChange = { vm.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation()
        )
        TextField(
            value = vm.birthyear,
            onValueChange = { vm.birthyear = it },
            label = { Text("Year of birth") },
            modifier = Modifier.fillMaxWidth(),
        )


        // Role selection for Tutor or Tutee
        Text("Select Role:")
        Row(modifier = Modifier.selectableGroup()) {
            // Tutor Option
            RadioButton(
                selected = vm.selectedRole == "Tutor",
                onClick = { vm.selectedRole = "Tutor" }
            )
            Text("Tutor", modifier = Modifier.padding(start = 8.dp))

            Spacer(modifier = Modifier.width(16.dp))

            // Tutee Option
            RadioButton(
                selected = vm.selectedRole == "Tutee",
                onClick = { vm.selectedRole = "Tutee" }
            )
            Text("Tutee", modifier = Modifier.padding(start = 8.dp))
        }

        Button(
            onClick = { vm.registerUser(vm.email, vm.password, vm.selectedRole, vm.birthyear) { success ->
                if (success) {
                    vm.isUserLoggedIn = true
                    // Fetch the user's email and role from Firestore after login
                    vm.userEmail = vm.email
                    vm.fetchUserRole { role ->
                        vm.userRole = role
                    }
                }
                vm.statusMessage = if (success) "Registration successful!" else "Registration failed"
            }},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        TextButton(
            onClick = { vm.isSignUpPage = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Already have an account? Log In")
        }
    } else {
        // Show Login page
        Text("Login")

        TextField(
            value = vm.email,
            onValueChange = { vm.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = vm.password,
            onValueChange = { vm.password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
        )

        Button(
            onClick = {
                vm.loginUser(vm.email, vm.password) { success ->
                    if (success) {
                        vm.isUserLoggedIn = true
                        // Fetch the user's email and role from Firestore after login
                        vm.userEmail = vm.email
                        vm.fetchUserRole { role ->
                            vm.userRole = role
                        }
                    }
                    vm.statusMessage = if (success) "Login successful!" else "Login failed"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        TextButton(
            onClick = { vm.isSignUpPage = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account? Sign Up")
        }
    }
}