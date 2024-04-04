package com.juanhegi.fantasticbooks.ui.books

object BookItem {
    data class Book(
        var id: Int = 0,
        var title: String = "",
        var author: String = "",
        var genre: String = "",
        var isbn: String = "",
        var datePublication: String = "",
        var publisher: String = "",
        var description: String = "",
        var numPages: Int? = 0,
        var imagenSrc: String = "",
        var available: Boolean = true,
        var removed: Boolean = false
    )

}