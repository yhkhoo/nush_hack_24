package com.example.nush_hack_24

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

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
