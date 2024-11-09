package com.example.nush_hack_24

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlin.math.round

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TutorScreen(
    vm: MainViewModel = viewModel()
) {
    val pagerState =
        rememberPagerState(initialPage = 0, pageCount = { 3 }) // Remember the pager state

    // Titles for the tabs
    val titles = listOf("About", "Requests", "Chat")
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar with a logout icon at the left
        TopAppBar(
            title = { Text("Tutor Profile") },
            navigationIcon = {
                IconButton(onClick = { vm.logoutUser { vm.isUserLoggedIn = false } }) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }
        )

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
                0 -> AboutScreen2(vm) // Show the About screen
                1 -> Requests() // Show the Chat screen
                2 -> ChatChoose()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen2(
    vm: MainViewModel = viewModel()
) {
    val scrollState = rememberScrollState()

    // State variables to hold the data
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var rev by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }

    val subjects2 = remember { mutableStateListOf<String>() }

    // Fetch user data from Firestore
    vm.db.collection("users").document(vm.userUid).get()
        .addOnSuccessListener { document ->
            name = document.get("name").toString()
            age = document.get("age").toString()
            bio = document.get("bio").toString()
            val num = document.get("rating")?.toString() ?: "0"
            val num2 = document.get("reviews")?.toString() ?: "0"
            val num3 = num.toFloat()
            val num4 = num2.toFloat()
            rev = if (num4.toInt() == 0) "No ratings yet." else (round(num3 * 10 / num4) / 10).toString()

            val subjects = document.get("subjects") as? List<*>
            subjects2.clear()
            subjects?.forEach {
                subjects2.add(it.toString())
            }
            subject = subjects?.joinToString(", ") ?: "No subjects available"

            // Update VM with the data
            vm.userName = name
            vm.userAge = age
            vm.userBio = bio
            vm.selectedSubjects = subjects2
        }
        .addOnFailureListener { exception ->
            Log.w("DocumentFields", "Error getting document: ", exception)
        }

    Column(modifier = Modifier.fillMaxSize()) {
        // TopAppBar with a logout icon at the left
        TopAppBar(
            title = { Text("Tutor Profile") },
            navigationIcon = {
                IconButton(onClick = { vm.logoutUser { vm.isUserLoggedIn = false } }) {
                    Icon(
                        imageVector = Icons.Filled.ExitToApp,
                        contentDescription = "Logout"
                    )
                }
            }
        )

        // Scrollable content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Text(
                "${vm.userName} ",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
            // Display Profile Data
            Text("Age: $age")
            Text("Bio: $bio")

            // Rating and star representation
            RatingWithStars(rating = rev)

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
        }
    }
}

@Composable
fun RatingWithStars(rating: String) {
    val ratingValue = rating.toFloatOrNull() ?: 0f
    val fullStars = ratingValue.toInt()
    val hasHalfStar = (ratingValue - fullStars) >= 0.5f
    val emptyStars = 5 - fullStars - if (hasHalfStar) 1 else 0

    Row(verticalAlignment = Alignment.CenterVertically) {
        // Display filled stars
        repeat(fullStars) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = "Full Star",
                modifier = Modifier.padding(2.dp)
            )
        }

        // Display half star (if any)
        if (hasHalfStar) {
            Icon(
                imageVector = Icons.Filled.StarBorder, // No half star icon in material, using border as placeholder
                contentDescription = "Half Star",
                modifier = Modifier.padding(2.dp)
            )
        }

        // Display empty stars
        repeat(emptyStars) {
            Icon(
                imageVector = Icons.Filled.StarBorder,
                contentDescription = "Empty Star",
                modifier = Modifier.padding(2.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp)) // Space between stars and rating text

        // Display the numeric rating
        Text(
            text = "($rating)",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
