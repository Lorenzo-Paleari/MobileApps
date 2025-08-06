package com.example.rssnews

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rssnews.databinding.ItemNewsBinding

//display objects, bind data and handle click events
class NewsAdapter(
    private val onItemClick: (Article) -> Unit, //metterà la funzione openArticle()
    private val onFavoriteClick: (Article) -> Unit //metterà la funzione toggleFavorite()
) : ListAdapter<Article, NewsAdapter.NewsViewHolder>(NewsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val binding = ItemNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NewsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NewsViewHolder(
        private val binding: ItemNewsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            // click listener per l'articolo, chiama openArticle
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            // click listener per il cuore, chiama toggleFavorite
            binding.favoriteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoriteClick(getItem(position))
                }
            }
        }

        //funzione per mettere i dati dell'articolo nel blocco
        fun bind(article: Article) {
            binding.apply {
                // titolo e descrizione
                newsTitle.text = article.title
                newsDescription.text = article.description

                // immagine
                Glide.with(newsImage)
                    .load(article.imageUrl)
                    .centerCrop()
                    .into(newsImage)

                // cuore pieno o vuoto
                favoriteButton.setImageResource(
                    if (article.isFavorite) R.drawable.ic_favorite
                    else R.drawable.ic_favorite_border
                )
                favoriteButton.setColorFilter(
                    if (article.isFavorite) android.graphics.Color.BLACK
                    else android.graphics.Color.GRAY
                )

                // articolo visualizzato
                root.alpha = if (article.isRead) 0.6f else 1.0f
            }
        }
    }

    //ottimizzare, carica solo gli oggetti cambiati
    private class NewsDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
} 