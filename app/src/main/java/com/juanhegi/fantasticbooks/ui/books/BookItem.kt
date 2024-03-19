package com.juanhegi.fantasticbooks.ui.books

object BookItem {
    data class Book(
        val id: Int = 0,
        val title: String = "",
        val author: String = "",
        val genre: String = "",
        val isbn: String = "",
        val datePublication: String = "",
        val publisher: String = "",
        val description: String = "",
        val numPages: Int = 0,
        val imagenSrc: String = "",
        val available: Boolean = true,
        val removed: Boolean = false
    )

}