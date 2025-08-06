// ottimizza l'aggiornamento della lista
// confrontando ed evitando di ricaricare tutta la lista
package com.example.filmworkappproject.adapters

import androidx.recyclerview.widget.DiffUtil
import com.example.filmworkappproject.model.Film

class DiffUtilCallback(
    val old: List<Film>,
    val new: List<Film>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    //controlla se l'oggetto in posizione old è lo stesso di quello in posizione new
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition] === new[newItemPosition] //stesso oggetto

    //se le due posizioni hanno film uguali
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        old[oldItemPosition] == new[newItemPosition] //stessi dati
}