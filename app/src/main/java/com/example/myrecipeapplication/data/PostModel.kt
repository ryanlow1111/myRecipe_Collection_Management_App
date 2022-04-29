package com.example.myrecipeapplication.data

import com.google.firebase.Timestamp

data class PostModel(
    val title: String = "",
    val description: String = "",
    val details: String = "",
    val image: String = "",
    val type: String = "",
    val public: Boolean = true,
    val user: String = "",
    val timestamp: Timestamp? = null
)
