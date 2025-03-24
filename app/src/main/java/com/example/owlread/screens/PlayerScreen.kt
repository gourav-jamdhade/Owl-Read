@file:Suppress("UNCHECKED_CAST")

package com.example.owlread.screens

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.owlread.R
import com.example.owlread.media.AudioPlayerService
import com.example.owlread.navigation.Screen
import com.example.owlread.viewmodel.ChapterViewModel
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun PlayerScreen(
    audiobookId: Int,
    chapterIndex: Int,
    currentPosition: Long, // Add currentPosition as a parameter
    //viewModel: ChapterViewModel = viewModel(),
    navController: NavController,
    viewModelStoreOwner: ViewModelStoreOwner
) {

    val context = LocalContext.current
    val viewModel: ChapterViewModel = viewModel(
        viewModelStoreOwner,
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ChapterViewModel(context.applicationContext as Application) as T
            }
        }
    )



    val imageUrl by viewModel.imageUrl.collectAsState()
    val chapters by viewModel.chapters.collectAsState()

    var isPlaying by remember { mutableStateOf(false) }
    var playbackPosition by remember { mutableStateOf(currentPosition) }  // Track progress
    var service: AudioPlayerService? by remember { mutableStateOf(null) }

// Bind to the service
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
                val localBinder = binder as AudioPlayerService.LocalBinder
                service = localBinder.getService()

                // Restore playback state after the service is connected
                service?.let {
                    it.seekTo(playbackPosition)
                    if (isPlaying) {
                        it.play()
                    } else {
                        it.pause()
                    }
                }

                isPlaying = service?.isPlaying() ?: false
                playbackPosition = service?.getCurrentPosition() ?: 0L

                Log.d(
                    "PlayerScreen",
                    "Service connected: isPlaying=$isPlaying, position=$currentPosition"
                )
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                service = null
            }
        }
    }

// Start and bind the service
    LaunchedEffect(Unit) {
        val audioUrl = chapters?.getOrNull(chapterIndex)?.enclosure?.url ?: ""
        AudioPlayerService.startService(context, audioUrl, audiobookId, chapterIndex)
        val intent = Intent(context, AudioPlayerService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    //val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    LaunchedEffect(service) {
        while (true) {
            playbackPosition = service?.getCurrentPosition() ?: 0L
            delay(500)  // Update every half second
        }
    }

    Log.d("PlayerScreen", "PlayerScreen currentPos: $playbackPosition")
    Log.d("PlayerScreen", "PlayerScreen service currentPos: ${service?.getCurrentPosition()}")






//    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT).apply {
//        setOnAudioFocusChangeListener { focusChange ->
//            when (focusChange) {
//                AudioManager.AUDIOFOCUS_LOSS -> {
//                    // Another app took focus permanently (e.g., phone call)
//                    isPlaying = false  // Pause playback
//                }
//
//                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
//                    // Temporarily lost focus (e.g., notification sound)
//                    isPlaying = false  // Pause playback
//                }
//
//                AudioManager.AUDIOFOCUS_GAIN -> {
//                    // Regained focus after being lost
//                    isPlaying = true
//                }
//            }
//        }
//    }.build()




    // Unbind the service when the composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            context.unbindService(serviceConnection)
        }
    }

    // Control playback
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            service?.play()
        } else {
            service?.pause()
        }
    }

    // Update current position
    LaunchedEffect(Unit) {
        while (true) {
            playbackPosition = service?.getCurrentPosition() ?: 0L
            delay(500)
        }
    }
    //val isLoading by viewModel.isLoading.collectAsState()
    // Fetch chapters when the screen loads
    LaunchedEffect(audiobookId) {
        if (chapters.isNullOrEmpty()) {
            Log.d("PlayerScreen", "Fetching chapters for audiobook ID: $audiobookId")

            viewModel.fetchChaptersByAudiobookId(audiobookId)
        } else {
            Log.d("PlayerScreen", "Using cached chapters for audiobook ID: $audiobookId")
        }

    }


    val selectedChapter = chapters?.getOrNull(chapterIndex)

    val title = selectedChapter?.title ?: "Unknown"
    val audioUrl = selectedChapter?.enclosure?.url ?: ""
    val duration = selectedChapter?.duration ?: ""
    Log.d("PlayerScreen", "PlayerScreen ImageUrl: $imageUrl")

// If chapters are still loading, show a loading indicator
    if (chapters == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    color = Color.Black,

                    )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading Chapter",
                    color = Color.Black,
                    fontWeight = MaterialTheme.typography.bodyLarge.fontWeight,
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize
                )
            }

        }
        return
    }


    // Format duration function
    fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    Log.d("PlayerScreen", "Selected Chapter: $title")


    //Exoplayer Builder
//    val exoPlayer = remember {
//        ExoPlayer.Builder(context).build()
//    }


    // Update progress continuously
    LaunchedEffect(service) {
        while (true) {
            playbackPosition = service?.getCurrentPosition() ?: 0
            delay(500)  // Update every half second
        }
    }


//    // Delay initialization until `audioUrl` is available
//    LaunchedEffect(chapters) {
//        if (chapters?.isNotEmpty() == true) {
//            Log.d("PlayerScreen Exoplayer", "Initializing audio: $audioUrl")
//            val mediaItem = MediaItem.fromUri(audioUrl)
//            exoPlayer.setMediaItem(mediaItem)
//            exoPlayer.prepare()
//        }
//    }


    // Handle next chapter navigation
    fun skipNext() {
        val nextChapter = viewModel.getNextChapter(chapterIndex)
        if (nextChapter != null) {
            val nextIndex = chapterIndex + 1
            navController.navigate(
                Screen.Player.createRoute(
                    audiobookId = audiobookId,
                    chapterIndex = nextIndex,
                    currentPosition = 0
                )
            ) {
                popUpTo(Screen.Player.route) { inclusive = true }
            }
        }
    }

    // Handle previous chapter navigation
    fun skipPrevious() {
        val previousChapter = viewModel.getPreviousChapter(chapterIndex)
        if (previousChapter != null) {
            val prevIndex = chapterIndex - 1
            navController.navigate(
                Screen.Player.createRoute(
                    audiobookId = audiobookId,
                    chapterIndex = prevIndex,
                    currentPosition = 0
                )
            ) {
                popUpTo(Screen.Player.route) { inclusive = true }
            }
        }
    }

    // Continuously update progress
    var sliderPosition by remember { mutableStateOf(0f) }
    var durationMs by remember { mutableStateOf(1L) }


    // Update progress when the player updates
    LaunchedEffect(sliderPosition) {
        Log.d("PlayerScreen AudioPlayerService", "Slider Position LauncedEffect: $sliderPosition")
        durationMs = service?.getDuration() ?: 1L
    }





//    LaunchedEffect(Unit) {
//        exoPlayer.addListener(object : Player.Listener {
//            override fun onPlaybackStateChanged(state: Int) {
//                if (state == Player.STATE_ENDED) {
//                    isPlaying = false  // Change pause button to play
//
//
//                    // Auto-load next chapter if available
//                    if (chapterIndex in 0 until (chapters?.size?.minus(1) ?: -1)) {
//                        navController.navigate(
//                            Screen.Player.createRoute(
//                                audiobookId,
//                                chapterIndex + 1,
//                                currentPosition = 0
//                            )
//                        ) {
//                            popUpTo(Screen.Player.route) { inclusive = true }
//                        }
//                    } else {
//                        Log.d("PlayerScreen", "No more chapters left.")
//                    }
//                }
//            }
//        })
//    }


// Sync slider with playback progress
    LaunchedEffect(service?.isPlaying()) {
        Log.d("PlayerScreen AudioPlayerService", "LaunchedEffect: $service")
        while (service?.isPlaying() == true) {
            sliderPosition = ((service?.getCurrentPosition()?.toFloat() ?: 0f) / durationMs)
            Log.d("PlayerScreen AudioPlayerService", "Slider Position: $sliderPosition")
            delay(500)  // Update every 500ms
        }
    }


// Debugging: Print values
    Log.d("PlayerScreen", "Title: ${title}, URL: ${audioUrl}, Duration: ${duration}")
//LaunchedEffect(isPlaying) {
//    if (isPlaying) {
//        val result = audioManager.requestAudioFocus(focusRequest)
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            Log.d("PlayerScreen Exoplayer", "Playing audio: $audioUrl")
//            //exoPlayer.play()
//        } else {
//            Log.e("PlayerScreen", "Audio focus request failed")
//        }
//    } else {
//        Log.d("PlayerScreen Exoplayer", "Pausing audio: $audioUrl")
//        //exoPlayer.pause()
//    }
//}

//DisposableEffect(Unit) {
//    onDispose {
//        //exoPlayer.release()
//        audioManager.abandonAudioFocusRequest(focusRequest)
//    }
//}

    val speedOptions = listOf(1f, 1.25f, 1.5f, 1.75f, 2f)  // Available speeds
    var selectedSpeed by remember { mutableStateOf(1f) }  // Default speed
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        GlideImage(
            model = if (imageUrl.isNullOrEmpty()) {
                painterResource(R.drawable.coverart_placeholder)
            } else {
                imageUrl
            },
            contentDescription = "Cover Image",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(.3f)
                .padding(horizontal = 24.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp),
            failure = placeholder(R.drawable.coverart_placeholder),
            loading = placeholder(R.drawable.loading_placeholder)

        )


        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Clip,
                modifier = Modifier.basicMarquee(),
                fontSize = MaterialTheme.typography.titleLarge.fontSize
            )
        }

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "${formatTime(playbackPosition)}/${duration.trimStart()}",
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                fontWeight = FontWeight.Bold,
                color = Color.Black.copy(alpha = .7f)

            )
        )
        Spacer(modifier = Modifier.height(25.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            TextButton(
                onClick = {
                    val currentIndx = speedOptions.indexOf(selectedSpeed)
                    val newIndex = (currentIndx + 1) % speedOptions.size
                    selectedSpeed = speedOptions[newIndex]
                    service?.setPlaybackSpeed(selectedSpeed)
                },
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(10.dp)
            ) {
                Image(
                    painter = painterResource(R.drawable.baseline_speed_24),
                    contentDescription = "Speed Logo",
                    modifier = Modifier.padding(end = 8.dp)
                )

                Text(
                    text = "${selectedSpeed}x",
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp)
        ) {


            Image(
                painter = painterResource(R.drawable.baseline_replay_10_24),
                contentDescription = "replay 10 sec",
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = .4f),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(8.dp)
                    .weight(.1f)
                    .clickable {
                        service?.seekBack()
                    }
            )


            Button(
                onClick = {
                    isPlaying = !isPlaying
                },
                modifier = Modifier
                    .fillMaxWidth(.7f)
                    .height(60.dp)
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.outline
                )
            ) {
                Image(
                    painter = if (isPlaying) {
                        painterResource(R.drawable.baseline_pause_24)
                    } else {
                        painterResource(R.drawable.baseline_play_arrow_24)
                    }, contentDescription = "play/pause button", modifier = Modifier.size(40.dp)
                )
            }


            Image(
                painter = painterResource(R.drawable.baseline_forward_10_24),
                contentDescription = "replay 10 sec",
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = .4f),
                        shape = CircleShape
                    )
                    .padding(8.dp)
                    .weight(.1f)
                    .clickable {
                        service?.seekForward()
                    }
            )

        }

        Spacer(modifier = Modifier.height(50.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 8.dp),

            verticalAlignment = Alignment.CenterVertically
        ) {


            Image(
                painter = painterResource(R.drawable.baseline_skip_previous_24),
                contentDescription = "skip previous",
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = .4f),
                        shape = RoundedCornerShape(100.dp)
                    )
                    .padding(8.dp)
                    .weight(.1f)
                    .clickable {
                        skipPrevious()

                    }
            )


            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                },
                onValueChangeFinished = {
                    val newPosition = (sliderPosition * durationMs).toLong()
                    Log.d("PlayerScreen AudioPlayerService", "New Position: $newPosition")
                    service?.seekTo(newPosition)
                },
                modifier = Modifier
                    .fillMaxWidth(.7f)
                    .height(10.dp)
                    .padding(horizontal = 8.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.Black,
                    activeTrackColor = Color.Black,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = .4f)
                ),

                )


            Image(
                painter = painterResource(R.drawable.baseline_skip_next_24),
                contentDescription = "skip next",
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = .4f),
                        shape = CircleShape
                    )
                    .padding(8.dp)
                    .weight(.1f)
                    .clickable {
                        skipNext()


                    }
            )

        }

    }


}


//@Preview(showBackground = true)
//@Composable
//
//fun Preview() {
//    PlayerScreen()
//}