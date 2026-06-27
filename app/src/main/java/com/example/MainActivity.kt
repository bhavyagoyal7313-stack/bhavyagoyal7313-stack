package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.data.DoubtRepository
import com.example.data.local.AppDatabase
import com.example.ui.TutorViewModel
import com.example.ui.TutorViewModelFactory
import com.example.ui.screens.MainTutorScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize local Room db and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = DoubtRepository(database.doubtDao)

        // 2. Instantiate viewmodel
        val viewModel = ViewModelProvider(
            this,
            TutorViewModelFactory(repository)
        )[TutorViewModel::class.java]

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainTutorScreen(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
