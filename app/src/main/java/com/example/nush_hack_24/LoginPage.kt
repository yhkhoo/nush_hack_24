package com.example.nush_hack_24

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginPage(vm: MainViewModel = viewModel()) {
    // Wrap the content in a Box to center it
    Box(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(16.dp), // Add some padding around the Box
    ) {
        // Center the column with either login or sign up content
        Column(
            modifier = Modifier
                .align(Alignment.Center) // This centers the Column in the Box
                .fillMaxWidth(), // Ensure the column takes full width
            horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
            verticalArrangement = Arrangement.spacedBy(8.dp) // Space between the elements
        ) {
            // Show Login or Sign-Up page if the user is not logged in
            if (vm.isSignUpPage) {
                // Show Sign-Up page
                Text("Sign Up", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)

                // Name field (newly added)
                TextField(
                    value = vm.userName,  // This binds the TextField value to vm.userName
                    onValueChange = { vm.userName = it },  // Update vm.userName with the user's input
                    label = { Text("Full Name") },  // Label for the name field
                    modifier = Modifier.fillMaxWidth()  // Make it take up full width
                )

                // Email field
                TextField(
                    value = vm.email,
                    onValueChange = { vm.email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Password field
                TextField(
                    value = vm.password,
                    onValueChange = { vm.password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation()  // Hide password text
                )

                // Age field
                TextField(
                    value = vm.birthyear,
                    onValueChange = { vm.birthyear = it },
                    label = { Text("Age") },
                    modifier = Modifier.fillMaxWidth(),
                )

                // Role selection for Tutor or Tutee
                Text("Select Role:", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                Row(modifier = Modifier.selectableGroup().fillMaxWidth().padding(end = 16.dp), horizontalArrangement = Arrangement.Center) {
                    // Tutor Option
                    RadioButton(
                        selected = vm.selectedRole == "Tutor",
                        onClick = { vm.selectedRole = "Tutor" }
                    )
                    Text("Tutor", modifier = Modifier.padding(start = 8.dp, top = 10.dp))

                    Spacer(modifier = Modifier.width(16.dp))

                    // Tutee Option
                    RadioButton(
                        selected = vm.selectedRole == "Tutee",
                        onClick = { vm.selectedRole = "Tutee" }
                    )
                    Text("Tutee", modifier = Modifier.padding(start = 8.dp, top = 10.dp))
                }

                // Sign-up Button
                Button(
                    onClick = {
                        // Call the registerUser function to register the user with the name, email, password, etc.
                        vm.registerUser(vm.email, vm.password, vm.selectedRole, vm.birthyear, vm.userName) { success ->
                            if (success) {
                                vm.isUserLoggedIn = true
                                // Fetch the user's email and role from Firestore after login
                                vm.userEmail = vm.email
                                vm.fetchUserRole { role -> vm.userRole = role }
                            }
                            vm.statusMessage = if (success) "Registration successful!" else "Registration failed"
                        }
                    },
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
                Text("Login", modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp), textAlign = TextAlign.Center)

                // Email field
                TextField(
                    value = vm.email,
                    onValueChange = { vm.email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Password field
                TextField(
                    value = vm.password,
                    onValueChange = { vm.password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                )

                // Login Button
                Button(
                    onClick = {
                        vm.loginUser(vm.email, vm.password) { success ->
                            if (success) {
                                vm.isUserLoggedIn = true
                                // Fetch the user's email and role from Firestore after login
                                vm.userEmail = vm.email
                                vm.fetchUserRole { role -> vm.userRole = role }
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
    }
}