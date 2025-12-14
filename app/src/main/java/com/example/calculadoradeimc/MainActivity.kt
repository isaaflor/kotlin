package com.example.calculadoradeimc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.calculadoradeimc.data.AppDatabase
import com.example.calculadoradeimc.view.DetailsScreen
import com.example.calculadoradeimc.view.HistoryScreen
import com.example.calculadoradeimc.view.Home
import com.example.calculadoradeimc.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = AppDatabase.getDatabase(applicationContext)
        val viewModel: MainViewModel by viewModels {
            MainViewModel.provideFactory(db.bmiDao())
        }

        enableEdgeToEdge()
        setContent {
            // 1. Create the NavController
            val navController = rememberNavController()

            // 2. Define the NavHost
            NavHost(navController = navController, startDestination = "home") {

                // Screen 1: Home
                composable("home") {
                    Home(
                        mainViewModel = viewModel,
                        onHistoryClick = { navController.navigate("history") }
                    )
                }

                // Screen 2: History
                composable("history") {
                    HistoryScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() },
                        onRecordClick = { recordId ->
                            // Navigate to Details with Argument
                            navController.navigate("details/$recordId")
                        }
                    )
                }

                // Screen 3: Details (with Argument)
                composable(
                    route = "details/{recordId}",
                    arguments = listOf(navArgument("recordId") { type = NavType.IntType })
                ) { backStackEntry ->
                    // Extract the ID from arguments
                    val id = backStackEntry.arguments?.getInt("recordId") ?: 0

                    DetailsScreen(
                        viewModel = viewModel,
                        recordId = id,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}