// mostra lista film
package com.example.filmworkappproject.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.filmworkappproject.databinding.ItemFilmBinding
import com.example.filmworkappproject.model.Film

// Per la lista dei film
class FilmView(val itemBinding: ItemFilmBinding) : RecyclerView.ViewHolder(itemBinding.root){

    // associa i dati del film alla view dell'oggetto
    fun onBind(film: Film) {
        with(itemBinding) {
            name.text = film.name
            category.text = film.category
            releaseDate.text = film.releaseDate
            status.text = if (film.seen) "Seen" else "Not Seen"

            if (!film.imageUri.isNullOrBlank()) {
                imageView.setImageURI(Uri.parse(film.imageUri))
            } else {
                imageView.setImageResource(film.icon)
            }

            //se è visto e il commento non è vuoto lo metto visibile e lo metto
            if (film.seen && !film.comment.isNullOrBlank()) {
                comment.visibility = View.VISIBLE
                comment.text = film.comment
            } else {
                comment.visibility = View.GONE //altrimenti lo tolgo
            }
        }
    }
}

class FilmListAdapter(
        private val onClick: (Film) -> Unit,
        private val onLongPress: (Film) -> Unit
        ) : RecyclerView.Adapter<FilmView>() {

    var filmList = listOf<Film>()

    set(value){
        //field:vecchia, value:nuova, DiffUtilCallback dice come confrontarli, difference conterrà gli elementi aggiunti, rimossi o cambiati
        val difference = DiffUtil.calculateDiff(DiffUtilCallback(field, value))
        field = value //mette quella nuova
        difference.dispatchUpdatesTo(this) //aggiunge solo le modifiche fatte senza ricaricare tutto
    }

    //quando crei un oggetto film
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilmView {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFilmBinding.inflate(inflater, parent, false)

        //crea un FilmView con i listener che mandano l'id giusto, non quello riordinato
        return FilmView(binding).also { holder ->
            binding.root.setOnClickListener {
                val position = holder.layoutPosition
                if (position != RecyclerView.NO_POSITION) {
                    onClick(filmList[position])
                }
            }
            binding.root.setOnLongClickListener {
                val position = holder.layoutPosition
                if (position != RecyclerView.NO_POSITION) {
                    onLongPress(filmList[position])
                }
                true
            }
        }
    }

    override fun getItemCount(): Int = filmList.size //numero totale dei film

    override fun onBindViewHolder(holder: FilmView, position: Int) {
        holder.onBind(filmList[position]) //associa il film al viewholder
    }
}