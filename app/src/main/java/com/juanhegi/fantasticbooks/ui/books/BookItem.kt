package com.juanhegi.fantasticbooks.ui.books

object BookItem {
    data class Book(
        val id: Int,
        val title: String,
        val author: String,
        val genre: String,
        val isbn: String,
        val datePublication: String,
        val publisher: String,
        val description: String,
        val numPages: Int,
        val imageUrl: String,
        val available: Boolean,
        val removed: Boolean
    )

}