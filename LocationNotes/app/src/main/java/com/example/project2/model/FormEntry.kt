package com.example.project2.model

import android.net.Uri

data class FormEntry(
    val text: String,
    val imageUri: Uri,
    val townName: String,
    val audioUri: Uri? = null
)
