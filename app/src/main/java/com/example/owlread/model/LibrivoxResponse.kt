package com.example.owlread.model

import com.google.gson.annotations.SerializedName

data class LibrivoxResponse(
    @SerializedName("books") val books:List<Audiobook>
)
