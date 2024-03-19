package com.juanhegi.fantasticbooks.ui.books

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import com.juanhegi.fantasticbooks.databinding.FragmentBookBinding
import com.squareup.picasso.Picasso

class MybookRecyclerViewAdapter(
    private val values: List<BookItem.Book>
) : RecyclerView.Adapter<MybookRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            FragmentBookBinding.inflate(
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
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(binding: FragmentBookBinding) : RecyclerView.ViewHolder(binding.root) {
        val author: TextView = binding.bookAuthor
        val title: TextView = binding.bookTitle
        val image: ImageView = binding.bookPortada
    }

}