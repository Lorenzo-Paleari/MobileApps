package com.example.filmworkappproject.model

import androidx.annotation.DrawableRes
import java.io.Serializable

data class Film(
    val name: String,
    @DrawableRes val icon: Int, //se in memoria
    val releaseDate: String = "undefined",
    val category: String = "undefined",
    val seen: Boolean = false,
    val comment: String? = null,
    val imageUri: String? = null //se selezionata dalla galleria
) : Serializable
