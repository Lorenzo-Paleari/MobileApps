package com.example.filmworkappproject.data

import com.example.filmworkappproject.R
import com.example.filmworkappproject.model.Film

object InMemoryFilmRepository : FilmRepository {

    // tutti i film
    private val filmListHolder = mutableListOf(
        Film(
            name = "Una notte da Leoni",
            icon = R.drawable.unanottedaleoni,
            releaseDate = "2009",
            category = "Film",
            seen = true,
            comment = "Realy funny!"
        ),
        Film(
            name = "Mucce alla riscossa",
            icon = R.drawable.muccheallariscossa,
            releaseDate = "2004",
            category = "Film",
            seen = false,
            comment = ""
        ),
        Film(
            name = "Fight Club",
            icon = R.drawable.fightclub,
            releaseDate = "1999",
            category = "Film",
            seen = true,
            comment = "Must watch"
        ),
        Film(
            name = "I soliti idioti",
            icon = R.drawable.isolitiidioti,
            releaseDate = "2011",
            category = "Film",
            seen = true,
            comment = "true italian brainrot"
        ),
        Film(
            name = "Matrix",
            icon = R.drawable.matrix,
            releaseDate = "1999",
            category = "Film",
            seen = true,
            comment = "story"
        ),
        Film(
            name = "Sole a catinelle",
            icon = R.drawable.soleacatinelle,
            releaseDate = "2013",
            category = "Film",
            seen = false,
            comment = ""
        ),
        Film(
            name = "Stitch",
            icon = R.drawable.stitch,
            releaseDate = "2002",
            category = "Film",
            seen = true,
            comment = "<3"
        ),
        Film(
            name = "The Truman Show",
            icon = R.drawable.thetrumanshow,
            releaseDate = "1998",
            category = "Film",
            seen = true,
            comment = "iconic"
        ),
        Film(
            name = "The Founder",
            icon = R.drawable.thefounder,
            releaseDate = "2016",
            category = "Documentary",
            seen = false,
            comment = ""
        ),
        Film(
            name = "Love, Death & Robots",
            icon = R.drawable.lovedeathrobots,
            releaseDate = "2019",
            category = "Series",
            seen = true,
            comment = "mind blowing"
        )
    )

    //da una copia di tutti i film
    override fun getFilmList(): List<Film> = filmListHolder.toList()

    //aggiunge un film
    override fun addFilm(film: Film) {
        filmListHolder.add(film)
    }

    //prende il film in posizione id
    override fun getFilm(id: Int): Film = filmListHolder[id]

    //modifica un film, dopo aver premuto il tasto save
    override fun edit(oldFilm: Film, newFilm: Film) {
        val index = filmListHolder.indexOf(oldFilm)
        if (index != -1) {
            filmListHolder[index] = newFilm //mette nell'indice del vecchio film quello nuovo
        }
    }

    //toglie un film
    override fun remove(Id: Film) {
        filmListHolder.remove(Id)
    }
}