package com.example.nush_hack_24

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TuteeScreen(
    vm: MainViewModel = viewModel()
) {
    val pagerState =
        rememberPagerState(initialPage = 0, pageCount = { 2 }) // Remember the pager state

    // Titles for the tabs
    val titles = listOf("About", "Chat")
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // TabRow for tab navigation
        TabRow(selectedTabIndex = pagerState.currentPage) {
            titles.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.scrollToPage(index)
                        }
                    }
                )
            }
        }

        // HorizontalPager to display the content based on the selected tab
        HorizontalPager(
            state = pagerState // Explicitly set the number of pages (equal to titles size)
        ) { page ->
            when (page) {
                0 -> AboutScreen(vm) // Show the About screen
                1 -> ChatChoose() // Show the Chat screen
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(vm: MainViewModel) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(16.dp)
            .verticalScroll(scrollState),

        ) {
        Text(
            "Logged in as: ${vm.userEmail} (TUTEE)",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display Profile Data
        Text("Name: ${vm.userName}")
        Text("Age: ${vm.userAge}")
        Text("Bio: ${vm.userBio}")
        Text("Subjects: ${vm.selectedSubjects.joinToString(", ")}")

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
            EditPage()
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Logout Button
        Button(
            onClick = { vm.logoutUser { vm.isUserLoggedIn = false } },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout")
        }

        Column() {
            // State for selected option
            var selectedOption by remember { mutableStateOf("English") }

            // State for showing the dropdown
            var expanded by remember { mutableStateOf(false) }

            // Handle item selection
            fun onOptionSelected(option: String) {
                selectedOption = option
                expanded = false  // Close the dropdown after selection
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // TextField to display selected option and open dropdown
                TextField(
                    value = selectedOption,
                    onValueChange = { selectedOption = it },
                    label = { Text("Select Option") },
                    readOnly = true,  // Make the TextField read-only, so user can only select from dropdown
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )

                // Dropdown menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    vm.subjects.forEach { option ->
                        DropdownMenuItem(
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier,
                            text = {
                                Text(text = option)
                            })
                    }
                }

                // Toggle the dropdown when the TextField is clicked
                Button(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Toggle Dropdown")
                }
            }
            Button(
                onClick = {
                    vm.searchTutors(selectedOption)
                    vm.isSearch = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search")
            }
        }
        Column() {
            vm.foundTutors.forEach { user ->
                Card(
                    modifier = Modifier.padding(4.dp).fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(4.dp)){
                        Text(user.name, style = MaterialTheme.typography.titleMedium)
                        Text("Bio: ${user.bio}")
                        Text("Age: ${user.age}")
                        Text("Email: ${user.email}")
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
