package com.juanhegi.fantasticbooks.ui.search

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.databinding.FragmentSearchBinding
import com.juanhegi.fantasticbooks.ui.books.BookItem
import com.squareup.picasso.Picasso

class MysearchRecyclerViewAdapter(
    private val values: List<BookItem.Book>,
    private val navController: NavController
) : RecyclerView.Adapter<MysearchRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            FragmentSearchBinding.inflate(
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
        if (item.imagenSrc.isNotEmpty()) {
            Picasso.get().load(item.imagenSrc).into(holder.image)
        }
        holder.itemView.tag = item.id
        holder.itemView.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("book", item.id)
            }

            navController.navigate(R.id.action_nav_search_to_book_detail, bundle)
        }
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentSearchBinding) : RecyclerView.ViewHolder(binding.root) {
        val author: TextView = binding.bookAuthorSearch
        val title: TextView = binding.bookTitleSearch
        val image: ImageView = binding.bookPortadaSearch
    }

}