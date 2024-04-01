package com.juanhegi.fantasticbooks.ui.lend_return

import com.google.firebase.Timestamp
import java.util.Calendar

object Lend {
    data class LendBook(
        var userEmail: String = "",
        var bookId: Int = 0,
        var loanDate: Timestamp = Timestamp.now(),
        var returnDate: Timestamp = Timestamp.now()
    ) {
        init {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = loanDate.seconds * 1000
            calendar.add(Calendar.DAY_OF_MONTH, 31)
            returnDate = Timestamp(calendar.timeInMillis / 1000, 0)
        }
    }
}