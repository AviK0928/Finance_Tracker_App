package com.example.finance_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.example.finance_tracker.core.navigation.AppNavGraph
import com.example.finance_tracker.core.navigation.NavigationActions
import com.example.finance_tracker.core.ui.theme.Finance_TrackerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Finance_TrackerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    val navActions = remember(navController) { NavigationActions(navController) }
                    AppNavGraph(navController = navController, navActions = navActions)
                }
            }
        }
    }
}

