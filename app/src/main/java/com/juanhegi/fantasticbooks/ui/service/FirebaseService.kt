package com.juanhegi.fantasticbooks.ui.service

import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore


class FirebaseService {
    private val db = Firebase.firestore
    fun getImageUrlsForGenre(genre: String, callback: (List<String>) -> Unit) {
        val listImagesUrl = mutableListOf<String>()
        db.collection("books")
            .whereEqualTo("genre", genre.lowercase())
            .limit(4)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    //Log.d(TAG, "${document.id} => ${document.data} => ${document.getString("imagenSrc")}")
                    val imageUrl = document.getString("imagenSrc")
                    if (imageUrl != null) {
                        listImagesUrl.add(imageUrl)

                    }
                }
                callback(listImagesUrl)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
                callback(emptyList()) // Llama al callback con una lista vacía en caso de error
            }
    }
}