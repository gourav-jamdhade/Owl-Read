package com.example.owlread.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BottomControls(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBackward: () -> Unit,
    duration: String,
    onSeekTo: (Float) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {

        Slider(
            value = sliderPosition,
            onValueChange = {
                sliderPosition = it
            },
            onValueChangeFinished = {
                val newPosition = (sliderPosition * durationMs).toLong()
                exoPlayer.seekTo(newPosition)
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
    }
}