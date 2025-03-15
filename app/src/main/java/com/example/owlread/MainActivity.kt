package com.example.owlread

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.owlread.navigation.Screen
import com.example.owlread.screens.AudiobookListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            AppNavHost(navController)
        }
    }


}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.AudiobookList.route) {
        composable(Screen.AudiobookList.route) {
            AudiobookListScreen(navController)
        }

        composable(
            Screen.ChapterList.route,
            arguments = listOf(
                navArgument("bookTitle") { type = NavType.StringType },
                navArgument("rssUrl") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: ""
            val rssUrl = backStackEntry.arguments?.getString("rssUrl") ?: ""
            //ChapterListScreen(bookTitle,rssUrl)

        }
    }
}





