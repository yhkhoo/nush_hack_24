package com.example.nush_hack_24

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
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
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
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
        // TopAppBar with a logout icon at the left
        TopAppBar(
            title = { Text("Tutee Profile") },
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
            .verticalScroll(scrollState)
    ) {
        Text(
            vm.userName,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        // State variables to hold the data
        var name by remember { mutableStateOf("") }
        var age by remember { mutableStateOf("") }
        var bio by remember { mutableStateOf("") }

        Log.d("die", vm.userUid)

        vm.db.collection("users").document(vm.userUid).get()
            .addOnSuccessListener { document ->
                name = document.get("name").toString()
                age = document.get("age").toString()
                bio = document.get("bio").toString()
                vm.userName = name
                vm.userAge = age
                vm.userBio = bio
            }
            .addOnFailureListener { exception ->
                Log.w("DocumentFields", "Error getting document: ", exception)
            }

        // Display Profile Data
        Spacer(modifier = Modifier.height(16.dp))
        Text("Age: $age")
        Text("Bio: $bio")

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
        Column {
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
                // Row with TextField and dropdown icon inside
                Row(modifier = Modifier.fillMaxWidth()) {
                    // TextField to display selected option and open dropdown
                    TextField(
                        value = selectedOption,
                        onValueChange = { selectedOption = it },
                        label = { Text("Select Option") },
                        readOnly = true,  // Make the TextField read-only
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 8.dp),
                    )

                    // Dropdown toggle button (icon) placed inside the Row
                    IconButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text("▼") // Use an arrow icon or a custom one
                    }
                }

                // Dropdown menu
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    vm.subjects.forEach { option ->
                        DropdownMenuItem(
                            onClick = { onOptionSelected(option) },
                            modifier = Modifier,
                            text = {
                                Text(text = option)
                            }
                        )
                    }
                }
            }

            Button(
                onClick = {
                    vm.searchTutors(selectedOption)
                    vm.isSearch = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Search for Tutors")
            }
        }

        var showDialog by remember { mutableStateOf(false) }
        var selectedUser by remember { mutableStateOf<User?>(null) }

        Column {
            vm.foundTutors.forEach { user ->
                Card(
                    modifier = Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .clickable {
                            selectedUser = user
                            showDialog = true
                        }
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        Text(user.name, style = MaterialTheme.typography.titleMedium)
                        rateStars(user.rating)
                    }
                }
            }
        }

        if (showDialog && selectedUser != null) {
            TutorDialog(
                user = selectedUser!!,
                onDismissRequest = { showDialog = false },
                onHireClick = { /* Handle hire action here */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun TutorDialog(user: User, onDismissRequest: () -> Unit, onHireClick: () -> Unit) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Details", "Reviews")
    var isPending by remember { mutableStateOf(false) }
    var isConnect by remember { mutableStateOf(false) }

    // Call checkPending when the dialog opens
    LaunchedEffect(user.uid) {
        checkPending(user) { pending ->
            isPending = pending
        }
        checkConnect(user) { connect ->
            isConnect = connect
        }
    }

    val reviews = mutableStateListOf<Review>() // List of reviews for the selected user

    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(user.uid).collection("reviews")
        .get()
        .addOnSuccessListener { documents ->
            reviews.clear() // Clear any existing reviews
            for (document in documents) {
                val rating = document.getLong("rating")?.toInt() ?: 0
                val reviewText = document.getString("reviewText") ?: ""
                val review = Review(rating, reviewText)
                reviews.add(review)
            }
        }
        .addOnFailureListener { e ->
            Log.w("Review", "Error fetching reviews", e)
        }

    Dialog(onDismissRequest = onDismissRequest) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Tab Row
                TabRow(selectedTabIndex = selectedTab) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                checkPending(user) { isPending ->
                    if (isPending) {
                        // Handle the case where the current user is in the pending list
                        Log.d("checkPending", "User is pending")
                    } else {
                        // Handle the case where the current user is not in the pending list
                        Log.d("checkPending", "User is not pending")
                    }
                }
                checkConnect(user) { isConnect ->
                    if (isConnect) {
                        // Handle the case where the current user is in the pending list
                        Log.d("checkPending", "User is pending")
                    } else {
                        // Handle the case where the current user is not in the pending list
                        Log.d("checkPending", "User is not pending")
                    }
                }


                // Tab Content
                when (selectedTab) {
                    0 -> { // Details Tab
                        Column {
                            Text("Name: ${user.name}")
                            Text("Age: ${user.age}")
                            Text("Bio: ${user.bio}")
                            Text("Subjects: ${user.subjects.joinToString(", ")}")
                            Spacer(modifier = Modifier.height(24.dp))
                            if (isPending) {
                                Button(
                                    onClick = {onDismissRequest()},
                                    enabled = false,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Pending Request")
                                }
                            } else if (isConnect) {
                                Button(
                                    onClick = { onDismissRequest() },
                                    enabled = false,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Hired")
                                }
                            } else {
                                Button(
                                    onClick = {
                                        hireTutor(
                                            tutorId = user.uid,
                                            onSuccess = {
                                                isPending = true // Update pending status
                                                onDismissRequest()
                                            },
                                            onFailure = {
                                                // Handle failure if necessary
                                            }
                                        )
                                    },
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                ) {
                                    Text("Hire")
                                }
                            }
                        }
                    }
                    1 -> { // Reviews Tab
                        Column {
                            // Check if there are reviews
                            if (reviews.isEmpty()) {
                                Text(
                                    text = "No reviews yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            } else {
                                // Display reviews if there are any
                                reviews.forEach { review ->
                                    Text("Rating: ${review.rating}")
                                    Text("Comment: ${review.reviewText}")
                                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun checkPending(user: User, onResult: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    db.collection("users")
        .document(user.uid)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                // Access multiple fields
                val pending = (document.get("pending") as? List<String>) ?: listOf()
                onResult(currentUserId in pending)
            } else {
                Log.w("checkPending", "No such document")
                onResult(false) // Return false if no document exists
            }
        }
        .addOnFailureListener { exception ->
            Log.w("checkPending", "Error fetching document", exception)
            onResult(false) // Return false if there was an error
        }
}

fun checkConnect(user: User, onResult: (Boolean) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    db.collection("users")
        .document(user.uid)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                // Access multiple fields
                val connect = (document.get("connect") as? List<String>) ?: listOf()
                onResult(currentUserId in connect)
            } else {
                Log.w("checkConnect", "No such document")
                onResult(false) // Return false if no document exists
            }
        }
        .addOnFailureListener { exception ->
            Log.w("checkConnect", "Error fetching document", exception)
            onResult(false) // Return false if there was an error
        }
}

// Function to handle the "Hire" action
fun hireTutor(tutorId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {

    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    currentUserId?.let { userId ->
        val userDocRef = db.collection("users").document(userId)
        val tutorDocRef = db.collection("users").document(tutorId)

        db.runBatch { batch ->
            // Add tutorId to the user's "pending" list
            batch.update(userDocRef, "pending", FieldValue.arrayUnion(tutorId))

            // Add userId to the tutor's "pending" list
            batch.update(tutorDocRef, "pending", FieldValue.arrayUnion(userId))
        }.addOnSuccessListener {
            onSuccess()
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }
}
@Composable
fun rateStars(rating: String) {
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
