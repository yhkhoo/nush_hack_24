package com.example.nush_hack_24

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    // Initialize Firebase Auth and Firestore instances

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApp()
        }
    }


    @Composable
    fun MyApp() {
        val vm: MainViewModel = viewModel(this)

        // Main UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (vm.isUserLoggedIn) {
                when(vm.userRole){
                    "Tutee" -> TuteeScreen()
                    "Tutor" -> TutorScreen()
                    else -> AdminScreen()
                }
            } else {
                LoginPage()
            }
        }
    }


}
