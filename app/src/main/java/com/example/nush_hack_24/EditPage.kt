package com.example.nush_hack_24

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EditPage(subject: Boolean = false, vm: MainViewModel = viewModel()) {
    var nameState by remember { mutableStateOf(TextFieldValue(vm.userName)) }
    var bioState by remember { mutableStateOf(TextFieldValue(vm.userBio)) }
    var ageState by remember { mutableStateOf(TextFieldValue(vm.userAge.toString())) }
    Column(
        modifier = Modifier,
    ) {
        // Editable fields for profile
        Text("Edit Profile", style = MaterialTheme.typography.headlineLarge)

        // Name Field
        Text("Name")
        TextField(
            value = nameState,
            onValueChange = { nameState = it },
            label = { Text("Enter your name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Bio Field
        Text("Bio")
        TextField(
            value = bioState,
            onValueChange = { bioState = it },
            label = { Text("Enter your bio") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Age Field
        Text("Age")
        TextField(
            value = ageState,
            onValueChange = { ageState = it },
            label = { Text("Enter your age") },
            keyboardOptions = KeyboardOptions.Default.copy(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Subjects checkboxes
        Text("Select subjects")
        LazyRow(verticalAlignment = Alignment.CenterVertically) {
            items(vm.subjects) { subject ->
                Checkbox(
                    checked = vm.selectedSubjects.contains(subject),
                    onCheckedChange = { checked ->
                        if (checked) {
                            vm.selectedSubjects.add(subject)
                        } else {
                            vm.selectedSubjects.remove(subject)
                        }
                    }
                )
                Text(subject)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Profile Button
        Button(
            onClick = {
                vm.updateProfile(
                    nameState.text,
                    bioState.text,
                    (ageState.text.toIntOrNull() ?: 0).toString(),
                    vm.selectedSubjects
                ) { success ->
                    if (success) {
                        // Handle success
                        vm.isEditProfile = false
                    } else {
                        // Handle failure
                        Log.wtf("EditPage", "fuck")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Profile")
        }
    }
}