package com.example.owlread.navigation

import android.net.Uri
import com.example.owlread.viewmodel.ChapterViewModel


sealed class Screen(val route: String) {

    object AudiobookList : Screen("audiobook_list")
    object ChapterList:Screen("chapter_list/{audiobookId}/{bookTitle}/{rssUrl}"){
        fun createRoute(audiobookId: Int,bookTitle: String, rssUrl: String) = "chapter_list/$audiobookId/$bookTitle/$rssUrl"
    }


    object Player : Screen("player/{audiobookId}/{chapterIndex}") {
        fun createRoute(audiobookId: Int, chapterIndex: Int) =
            "player/$audiobookId/$chapterIndex"
    }



}