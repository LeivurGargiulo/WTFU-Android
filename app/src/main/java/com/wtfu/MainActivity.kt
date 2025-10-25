package com.wtfu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wtfu.ui.MainScreen
import com.wtfu.ui.ReliabilityGuideScreen
import com.wtfu.ui.theme.WTFUTheme

/**
 * Main activity for WTFU app.
 * Hosts navigation between main screen and reliability guide.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WTFUTheme {
                WTFUApp()
            }
        }
    }
}

@Composable
fun WTFUApp() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                onNavigateToReliability = {
                    navController.navigate("reliability")
                }
            )
        }
        composable("reliability") {
            ReliabilityGuideScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
