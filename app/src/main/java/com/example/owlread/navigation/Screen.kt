package com.example.owlread.navigation

sealed class Screen(val route: String) {

    object AudiobookList : Screen("audiobook_list")
    object ChapterList:Screen("chapter_list/{bookTitle}/{rssUrl}"){
        fun createRoute(bookTitle: String, rssUrl: String) = "chapter_list/$bookTitle/$rssUrl"
    }

}