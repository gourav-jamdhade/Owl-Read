package com.example.owlread

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.owlread.navigation.Screen
import com.example.owlread.screens.AudiobookListScreen
import com.example.owlread.screens.ChapterListScreen
import com.example.owlread.screens.PlayerScreen

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()

            AppNavHost(navController)
        }
    }


}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.AudiobookList.route) {
        composable(Screen.AudiobookList.route) {
            AudiobookListScreen(navController)
        }



        composable(
            route = Screen.ChapterList.route,
            arguments = listOf(
                navArgument("audiobookId") { type = NavType.IntType },
                navArgument("bookTitle") { type = NavType.StringType },
                navArgument("rssUrl") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val audiobookId = backStackEntry.arguments?.getInt("audiobookId") ?: -1
            val bookTitle = backStackEntry.arguments?.getString("bookTitle") ?: "Unknown"
            val rssUrl = backStackEntry.arguments?.getString("rssUrl") ?: ""

            ChapterListScreen(title = bookTitle, rssUrl = rssUrl, audiobookId = audiobookId, navController = navController)
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("audiobookId") { type = NavType.IntType },
                navArgument("chapterIndex") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val audiobookId = backStackEntry.arguments?.getInt("audiobookId") ?: -1
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex") ?: 0

            PlayerScreen(
                audiobookId = audiobookId,
                chapterIndex = chapterIndex,
                navController = navController
            )
        }
    }
}





