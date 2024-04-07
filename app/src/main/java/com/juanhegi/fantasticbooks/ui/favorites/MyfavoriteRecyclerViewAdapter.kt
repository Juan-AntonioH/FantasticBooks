package com.juanhegi.fantasticbooks.ui.favorites

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import com.juanhegi.fantasticbooks.R

import com.juanhegi.fantasticbooks.databinding.FragmentFavoriteBinding
import com.juanhegi.fantasticbooks.ui.books.BookItem
import com.squareup.picasso.Picasso

class MyfavoriteRecyclerViewAdapter(
    private val values: List<BookItem.Book>,
    private val navController: NavController
) : RecyclerView.Adapter<MyfavoriteRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentFavoriteBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.author.text = item.author
        holder.title.text = item.title
        if (item.imagenSrc!!.isNotEmpty()) {
            Picasso.get().load(item.imagenSrc).into(holder.image)
        }
        holder.itemView.tag = item.id
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("book", item.id)
            }

            navController.navigate(R.id.action_favoriteFragment_to_book_detail, bundle)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentFavoriteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val author: TextView = binding.bookAuthorFavorite
        val title: TextView = binding.bookTitleFavorite
        val image: ImageView = binding.bookPortadaFavorite
    }

}