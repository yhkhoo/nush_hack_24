package com.example.nush_hack_24

import YourAppTheme
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    // Initialize Firebase Auth and Firestore instances

    override fun onCreate(savedInstanceState: Bundle?) {

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT, Color.TRANSPARENT
            )
        )
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        setContent {
            val windowInsertController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsertController.hide(WindowInsetsCompat.Type.systemBars())
            YourAppTheme {
                MyApp()
            }
        }
    }


    @Composable
    fun MyApp() {
        val vm: MainViewModel = viewModel(this)
        val view = LocalView.current

        // Main UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (vm.isUserLoggedIn) {
                when(vm.userRole){
                    "Tutee" -> YourAppTheme { TuteeScreen() }
                    "Tutor" -> TutorScreen()
                    else -> AdminScreen()
                }
            } else {
                LoginPage()
            }
        }
    }


}
