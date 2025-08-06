package com.example.filmworkappproject.data

import com.example.filmworkappproject.model.Film

interface FilmRepository {
    fun getFilmList(): List<Film>
    fun addFilm(film: Film)
    fun getFilm(id:Int) : Film
    fun edit(id: Film, film: Film)
    fun remove(Id: Film)
}