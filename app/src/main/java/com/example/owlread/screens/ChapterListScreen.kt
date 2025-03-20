package com.example.owlread.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.example.owlread.R
import com.example.owlread.model.Chapter
import com.example.owlread.navigation.Screen
import com.example.owlread.utilities.ShimmerEffect
import com.example.owlread.viewmodel.ChapterViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterListScreen(
    audiobookId: Int,
    title: String,
    rssUrl: String,
    navController: NavController,
    viewModel: ChapterViewModel = viewModel()
) {


    val chapters by viewModel.chapters.observeAsState(emptyList())
    val imageUrl by viewModel.imageUrl.observeAsState(null)
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.error.observeAsState(null)

    LaunchedEffect(rssUrl) {
        viewModel.fetchChapters(rssUrl)
    }
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Row {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.basicMarquee(),
                        maxLines = 1
                    )
                }

            },
            navigationIcon = {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Back Button"
                    )
                }
            })
    }) { padding ->
        if (isLoading) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(7) {
                    ChapterShimmerItem()
                }
            }
        } else if (errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(errorMessage!!)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.fetchChapters(rssUrl) }) {
                    Text("Retry")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (chapters?.isEmpty() == true) {
                    items(7) {
                        ChapterShimmerItem()
                    }
                } else {
                    items(chapters!!) { chapter ->
                        ChapterItem(chapter, imageUrl) {
                            viewModel.selectChapter(chapter)
                            val chapterIndex = chapters!!.indexOf(chapter)
                            Log.d("ChapterListScreen", "Chapter Index: $chapterIndex")
                            try {
                                navController.navigate(
                                    Screen.Player.createRoute(
                                        audiobookId = audiobookId,
                                        chapterIndex = chapterIndex
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("ChapterListScreen", "Error navigating to Player: ${e}")
                            }

                        }
                    }
                }


            }
        }


    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ChapterItem(chapter: Chapter, imageUrl: String?, onClick: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable {
                onClick()
            },
    ) {
        Log.d("ChapterRepository", "ChapterItem: $imageUrl")
//        AsyncImage(
//            model = ImageRequest.Builder(LocalContext.current)
//                .data(imageUrl)
//                .crossfade(true)
//                .build(),
//            placeholder = painterResource(R.drawable.coverart_placeholder),
//            contentDescription = "Cover Image",
//            contentScale = ContentScale.Crop,
//            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(10.dp)),
//        )

        GlideImage(
            model = if (imageUrl.isNullOrEmpty()) {
                painterResource(R.drawable.coverart_placeholder)
            } else {
                imageUrl
            },
            contentDescription = "Cover Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(90.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(10.dp)
                )
                .padding(12.dp),
            failure = placeholder(R.drawable.coverart_placeholder),
            loading = placeholder(R.drawable.loading_placeholder),

            )

        Spacer(modifier = Modifier.width(8.dp))

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterVertically)
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = chapter.title,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.basicMarquee(),
                    fontSize = 16.sp
                )
            }

            Text(
                text = "Duration: ${chapter.duration.toString().trimStart()}",
                style = MaterialTheme.typography.bodySmall
            )

        }


    }



    HorizontalDivider(thickness = (1.5).dp)

}


@Composable
fun ChapterShimmerItem() {
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


        }


    }
}