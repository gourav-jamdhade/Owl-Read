package com.example.owlread.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.owlread.R
import com.example.owlread.model.Audiobook
import com.example.owlread.utilities.ShimmerEffect
import com.example.owlread.viewmodel.AudiobookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookListScreen(navController: NavController, viewModel: AudiobookViewModel = viewModel()) {

    val audiobooks by viewModel.audiobooks.observeAsState(emptyList())

    LaunchedEffect(Unit) {
        viewModel.fetchAudiobooks()
    }

    Log.d("AudiobookListScreen", "AudiobookListScreen: ${audiobooks.size}")
    Scaffold(

        topBar = {
            TopAppBar(title = {
                Text(
                    "Audiobooks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            })
        }
    ) { padding ->

//        LazyColumn(modifier = Modifier.padding(padding)) {
//            items(audiobooks) { audiobook ->
//                AudiobookItem(audiobook)
//
//            }
//        }

        LazyColumn(

            modifier = Modifier.padding(padding),

            ) {

            if (audiobooks.isEmpty()) {
                items(7) {
                    AudiobookShimmerItem()
                }
            } else {
                items(audiobooks) { book ->

                    AudiobookItem(book){
                        navController.navigate("chapter_list/${book.title}/${book.url_rss}")
                    }


                }
            }


        }

    }
}

@Composable
fun AudiobookItem(audiobook: Audiobook, onClick:()->Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp).clickable {
                onClick()
            }
    ) {

        Image(
            painter = painterResource(R.drawable.coverart_placeholder),
            contentDescription = "Cover of ${audiobook.title}",
            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)

        )


        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {
            audiobook.title?.let {
                Row {
                    Text(
                        text = it,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.basicMarquee()

                    )
                }
            }
            Text(
                text = "Author: ${audiobook.authors.joinToString(", ") { it.fullName }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Duration: ${audiobook.totaltime.toString()}",
                style = MaterialTheme.typography.bodySmall
            )


        }


    }
    HorizontalDivider(thickness = 1.dp)

}


@Composable
fun AudiobookShimmerItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {

        ShimmerEffect(

            modifier = Modifier
                .size(80.dp)
                .padding(8.dp)

        )


        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier.align(Alignment.CenterVertically)
        ) {

            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            ShimmerEffect(modifier = Modifier
                .fillMaxWidth(.6f)
                .height(12.dp))
            Spacer(modifier = Modifier.height(4.dp))

            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(.4f)
                    .height(10.dp)
            )


        }


    }
}
