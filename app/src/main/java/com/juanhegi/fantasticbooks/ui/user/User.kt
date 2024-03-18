package com.juanhegi.fantasticbooks.ui.user

import com.google.firebase.Timestamp
import com.google.type.Date

object User {
    data class User(
        val document: String = "",
        var name: String = "",
        var lastname: String = "",
        val email: String = "",
        var imageUrl: String? = null,
        val sanction: Timestamp? = null,
        val dischargeDate: Timestamp = Timestamp.now(), // Convertir LocalDate.now() a Timestamp
        val rol: String = "",
        val delete: Boolean = false
    )
}