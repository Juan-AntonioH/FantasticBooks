package com.juanhegi.fantasticbooks.ui.lend_return

import com.google.firebase.Timestamp

object ReturnBook {
    data class ReturnBook(
        var userEmail: String = "",
        var bookId: Int = 0,
        var returnDate: Timestamp = Timestamp.now()
    )
}