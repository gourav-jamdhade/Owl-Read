package com.example.owlread.model

import com.google.gson.annotations.SerializedName

data class Audiobook(
    val id: Int,
    val title: String?,
    val authors: List<Author>,
    val description: String?,
    val url_rss: String?,
    val url_image: String?,
    @SerializedName("totaltime") val totaltime: String?,
    val totaltimesecs: Int // Add this field

)


data class Author(
    val id: Int,
    val first_name: String,
    val last_name: String
) {
    val fullName: String
        get() = "$first_name $last_name"
}
