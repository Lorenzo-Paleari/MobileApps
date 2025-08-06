package com.example.filmworkappproject.model

import java.io.Serializable

//sealed perchè tutte le sottoclassi devono essere dichiarate dentro
sealed class FormType : Serializable{

    // per creare nuovo film
    data object New : FormType() {
        private fun readResolve(): Any = New
    }

    //per modificare un film esistente
    data class Edit(val film: Film) : FormType()
}