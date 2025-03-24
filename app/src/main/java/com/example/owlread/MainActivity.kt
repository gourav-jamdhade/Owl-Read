package com.example.owlread

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.owlread.media.AudioPlayerService
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
            AppNavHost(navController, viewModelStoreOwner = this)
// Check if the activity was launched from the notification


            LaunchedEffect(Unit) {
                try {


                    if (intent?.getBooleanExtra("redirect_to_player", false) == true) {
                        // Navigate to the PlayerScreen
                        val audiobookId = intent.getIntExtra("audiobookId", -1)
                        val chapterIndex = intent.getIntExtra("chapterIndex", 0)
                        // Get the current position from SharedPreferences
                        val sharedPreferences = getSharedPreferences("audio_player", MODE_PRIVATE)
                        val currentPosition = sharedPreferences.getLong("current_position", 0L)

                        Log.d(
                            "MainActivity",
                            "Reading from SharedPreferences: currentPosition=$currentPosition"
                        )
                        Log.d("MainActivity", "onCreate book id: $audiobookId")
                        Log.d("MainActivity", "onCreate index: $chapterIndex")

                        navController.navigate(
                            Screen.Player.createRoute(
                                audiobookId = audiobookId,
                                chapterIndex = chapterIndex,
                                currentPosition = currentPosition
                            )
//okay now as the notification is working we can now make our notification more useful by adding play/pause, skip next, skip previous, speed functionalities
                        ) // Replace with your PlayerScreen route
                    }
                } catch (e: Exception) {
                    Log.d("MainActivity", "onCreate: ${e.message}")
                }
            }

        }
    }


}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavHost(navController: NavHostController, viewModelStoreOwner: ViewModelStoreOwner) {
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

            ChapterListScreen(
                title = bookTitle,
                rssUrl = rssUrl,
                audiobookId = audiobookId,
                navController = navController,
                viewModelStoreOwner = viewModelStoreOwner
            )
        }

        composable(
            route = Screen.Player.route,
            arguments = listOf(
                navArgument("audiobookId") { type = NavType.IntType },
                navArgument("chapterIndex") { type = NavType.IntType },
                navArgument("currentPosition") { type = NavType.LongType } // Add currentPosition
            )
        ) { backStackEntry ->
            val audiobookId = backStackEntry.arguments?.getInt("audiobookId") ?: -1
            val chapterIndex = backStackEntry.arguments?.getInt("chapterIndex") ?: 0
            val currentPosition = backStackEntry.arguments?.getLong("currentPosition") ?: 0L

            PlayerScreen(
                audiobookId = audiobookId,
                chapterIndex = chapterIndex,
                navController = navController,
                viewModelStoreOwner = viewModelStoreOwner,
                currentPosition = currentPosition, // Pass currentPosition

            )
        }
    }
}





