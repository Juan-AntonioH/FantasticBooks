package com.juanhegi.fantasticbooks.ui.genre

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juanhegi.fantasticbooks.databinding.FragmentGenreBinding
import com.juanhegi.fantasticbooks.ui.service.FirebaseService
import com.squareup.picasso.Picasso


class MyGenreRecyclerViewAdapter() : RecyclerView.Adapter<MyGenreRecyclerViewAdapter.ViewHolder>() {
    //private val values: MutableList<Book> = mutableListOf()
    private val firebaseService = FirebaseService()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentGenreBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val genre = GenreItem.genres[position]
        holder.mGenero.text = GenreItem.genres[position]
        firebaseService.getImageUrlsForGenre(genre) { imageUrls ->
            // Aquí puedes acceder a los datos una vez que estén disponibles
           // holder.mGenero.text = if (imageUrls.isNotEmpty()) imageUrls[0] else "No hay imágenes disponibles"
            if (imageUrls.isNotEmpty()){
                Picasso.get().load(imageUrls[0]).into(holder.mPortada1)
                Picasso.get().load(imageUrls[1]).into(holder.mPortada2)
                Picasso.get().load(imageUrls[2]).into(holder.mPortada3)
                Picasso.get().load(imageUrls[3]).into(holder.mPortada4)
            }
        }
    }

    override fun getItemCount(): Int = GenreItem.genres.size

    inner class ViewHolder(binding: FragmentGenreBinding) : RecyclerView.ViewHolder(binding.root) {
        val mGenero: TextView = binding.genero
        val mPortada1: ImageView = binding.portada1
        val mPortada2: ImageView = binding.portada2
        val mPortada3: ImageView = binding.portada3
        val mPortada4: ImageView = binding.portada4

        override fun toString(): String {
            return super.toString() + " '"
        }
    }

}