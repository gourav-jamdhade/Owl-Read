package com.example.owlread.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.owlread.R
import com.example.owlread.model.Audiobook
import com.example.owlread.navigation.Screen
import com.example.owlread.utilities.ShimmerEffect
import com.example.owlread.viewmodel.AudiobookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudiobookListScreen(navController: NavController, viewModel: AudiobookViewModel = viewModel()) {

    val audiobooks by viewModel.audiobooks.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false) // Observe the loading state
    var showFilterDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {

        try {
            viewModel.fetchAudiobooks()
        } catch (e: Exception) {
            Log.d("AudiobookListScreen Exception", "Error: ${e.message}")
        }

    }

    Log.d("AudiobookListScreen", "AudiobookListScreen: ${audiobooks?.size}")
    Scaffold(

        topBar = {
            TopAppBar(title = {
                Text(
                    "Audiobooks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },

                navigationIcon = {
                    IconButton(
                        onClick = {

                        }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.baseline_menu_24),
                            contentDescription = "Side Menu"
                        )
                    }
                })
        }) { padding ->

        LazyColumn(modifier = Modifier.padding(padding)) {

            if (audiobooks.isEmpty()) {
                items(7) {
                    AudiobookShimmerItem()
                }
            } else {
                itemsIndexed(audiobooks) { index, book ->
                    Log.d("AudiobookListScreen", "Rendering book at index $index: ${book.title}")
                    AudiobookItem(book) {
                        Log.d("Navigation", "Clicked on book: ${book.totaltimesecs}")
                        try {
                            if (book.title != null && book.url_rss != null) {
                                Log.d("Navigation", Uri.encode(book.url_rss))
                                Log.d("Navigation", "${book.id}")
                                navController.navigate(
                                    Screen.ChapterList.createRoute(
                                        bookTitle = book.title,
                                        rssUrl = Uri.encode(book.url_rss),
                                        audiobookId = book.id
                                    )
                                )
                            } else {
                                Log.e("Navigation Error", "Invalid book data: $book")
                            }
                        } catch (e: Exception) {
                            Log.e("Exception Chapter", "${e.message}")
                        }
                    }

                    // Load more when the user scrolls to the last item
                    if (index == audiobooks.lastIndex && !isLoading) {
                        viewModel.fetchAudiobooks()
                    }
                }

                // Show a loading indicator at the bottom while fetching more books
                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }

        }

//        if (showFilterDialog) {
//            FilterSortDialog(
//                onFilterByAuthor = { author ->
//                    viewModel.resetFilters()
//                    viewModel.fetchAudiobooks(author = author)
//                    showFilterDialog = false
//                },
//                onSortByDuration = {
//                    viewModel.resetFilters()
//                    viewModel.fetchAudiobooks(sortByDuration = true)
//                    showFilterDialog = false
//                },
//                onResetFilters = {
//                    viewModel.resetFilters()
//                    viewModel.fetchAudiobooks()
//                    showFilterDialog = false
//                },
//                onDismiss = { showFilterDialog = false }
//            )
//        }
    }


}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun AudiobookItem(audiobook: Audiobook, onClick: () -> Unit) {
    if (audiobook == null) return // Skip rendering if the audiobook is null

    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp)
        .clickable {
            onClick()
        }) {


//        Log.d("Audiobook Screen", "AudiobookItem: $imageUrl")
        GlideImage(
            model = if (audiobook.url_image.isNullOrEmpty()) {
                R.drawable.coverart_placeholder
            } else {
                audiobook.url_image
            },
            contentDescription = "Cover Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(16.dp),
            failure = placeholder(R.drawable.coverart_placeholder),
            loading = placeholder(R.drawable.loading_placeholder)
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
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                        overflow = TextOverflow.Clip,
                        modifier = Modifier.basicMarquee()

                    )
                }
            }
            Text(
                text = "Authors: ${audiobook.authors.joinToString(", ") { it.fullName }}",
                style = MaterialTheme.typography.bodyMedium
            )

            Text(
                text = "Length: ${audiobook.totaltime.toString()}",
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

            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(.6f)
                    .height(12.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))

            ShimmerEffect(
                modifier = Modifier
                    .fillMaxWidth(.4f)
                    .height(10.dp)
            )


        }


    }
}


