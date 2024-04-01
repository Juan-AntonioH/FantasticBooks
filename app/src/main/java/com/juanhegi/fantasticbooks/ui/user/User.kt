package com.juanhegi.fantasticbooks.ui.user

import com.google.firebase.Timestamp

object User {
    data class User(
        val document: String = "",
        var name: String = "",
        var lastname: String = "",
        val email: String = "",
        var imageUrl: String? = null,
        var sanction: Timestamp? = null,
        val dischargeDate: Timestamp = Timestamp.now(),
        val rol: String = "",
        val delete: Boolean = false,
        var favorites: List<Int> = emptyList()
    )
}