package com.juanhegi.fantasticbooks.ui.service

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.juanhegi.fantasticbooks.ui.user.User
import java.util.concurrent.CompletableFuture


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

    @RequiresApi(Build.VERSION_CODES.N)
    fun saveUser(newUser: User.User, userId: String): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()
        val db = FirebaseFirestore.getInstance()
        val userDocument = db.collection("users").document(userId)

        userDocument.set(newUser)
            .addOnSuccessListener {
                result.complete(true)
            }
            .addOnFailureListener { e ->
                result.complete(false)
            }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateUser(user: User.User):CompletableFuture<Boolean>{
        val result = CompletableFuture<Boolean>()
        val db = FirebaseFirestore.getInstance()
        val userDocument = db.collection("users").document(user.document)
        val updates = mutableMapOf<String, Any>()

        user.name?.let { updates["name"] = it }
        user.lastname?.let { updates["lastname"] = it }
        user.imageUrl?.let { updates["imageUrl"] = it }

        userDocument.update(updates)
            .addOnSuccessListener {
                result.complete(true)
            }
            .addOnFailureListener { e ->
                result.complete(false)
            }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getUserData(userId: String): CompletableFuture<User.User?> {
        val result = CompletableFuture<User.User?>()
        val userDocument = db.collection("users").document(userId)

        userDocument.get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val userData = document.toObject(User.User::class.java)
                    result.complete(userData)
                } else {
                    // El documento del usuario no existe
                    result.complete(null)
                }
            }
            .addOnFailureListener { exception ->
                result.completeExceptionally(exception)
            }

        return result
    }
}