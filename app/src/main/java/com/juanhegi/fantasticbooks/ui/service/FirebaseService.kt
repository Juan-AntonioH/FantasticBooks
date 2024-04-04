package com.juanhegi.fantasticbooks.ui.service

import android.content.ContentValues.TAG
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.juanhegi.fantasticbooks.ui.books.BookItem
import com.juanhegi.fantasticbooks.ui.lend_return.Lend
import com.juanhegi.fantasticbooks.ui.lend_return.ReturnBook
import com.juanhegi.fantasticbooks.ui.user.User
import java.util.concurrent.CompletableFuture


class FirebaseService {
    private val db = Firebase.firestore

    //////////////////
    // Zona libros //
    /////////////////
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
                //  Log.w(TAG, "Error getting documents.", exception)
                callback(emptyList()) // Llama al callback con una lista vacía en caso de error
            }
    }

    fun getBooksByGenre(genre: String, callback: (List<BookItem.Book>) -> Unit) {
        val bookList = mutableListOf<BookItem.Book>()
        db.collection("books")
            .whereEqualTo("genre", genre.lowercase())
            .whereEqualTo("removed", false)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val book = document.toObject(BookItem.Book::class.java)
                    if (book != null) {
                        //Log.d(TAG, document.id)
                        bookList.add(book)
                    }
                }
                callback(bookList)
            }
            .addOnFailureListener { exception ->
                // Log.w(TAG, "Error getting documents.", exception)
                callback(emptyList()) // Llama al callback con una lista vacía en caso de error
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getFavoriteBooks(bookIds: List<Int>?, callback: (List<BookItem.Book>) -> Unit) {
        val favoriteBooks = mutableListOf<BookItem.Book>()
        if (bookIds != null && bookIds.isNotEmpty()) { // Verifica que bookIds no sea nulo ni esté vacío
            db.collection("books")
                .whereIn("id", bookIds)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val book = document.toObject(BookItem.Book::class.java)
                        favoriteBooks.add(book)
                    }
                    callback(favoriteBooks)
                }
                .addOnFailureListener {
                    callback(emptyList())
                }
        } else {
            callback(emptyList())
        }
    }

    fun getBooksById(id: Int, callback: (BookItem.Book?) -> Unit) {
        db.collection("books")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { result ->
                var book: BookItem.Book? = null
                for (document in result) {
                    book = document.toObject(BookItem.Book::class.java)
                    break // Solo necesitamos el primer libro con la ID específica
                }
                callback(book)
            }
            .addOnFailureListener { exception ->
                // Log.e(TAG, "Error getting documents", exception)
                callback(null) // Llamamos al callback con null para indicar un error
            }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun saveOrUpdateBook(book: BookItem.Book): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()
        val db = FirebaseFirestore.getInstance()

        if (book.id == 0) {
            // Si el id del libro es 0, buscamos el libro con el id más alto
            db.collection("books")
                .orderBy(
                    "id",
                    Query.Direction.DESCENDING
                ) // Ordenamos los libros por id descendente
                .limit(1) // Limitamos el resultado a 1 libro
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val lastBook = documents.documents[0].toObject(BookItem.Book::class.java)
                        // Obtenemos el último id y le sumamos 1 para obtener el nuevo id
                        val newId = lastBook!!.id + 1
                        // Actualizamos el id del libro
                        book.id = newId
                    }
                    // Guardamos el libro con el nuevo id
                    db.collection("books")
                        .add(book) // Utilizamos add() para añadir un nuevo documento
                        .addOnSuccessListener { documentReference ->
                            //Log.d(TAG, "Book added with ID: ${documentReference.id}")
                            result.complete(true)
                        }
                        .addOnFailureListener { e ->
                            // Log.e(TAG, "Error adding document", e)
                            result.complete(false)
                        }
                }
                .addOnFailureListener { exception ->
                    Log.e(TAG, "Error getting documents", exception)
                    result.complete(false)
                }
        } else {
            // Si el id del libro no es 0, actualizamos el libro existente
            db.collection("books")
                .whereEqualTo("id", book.id) // Buscamos el libro por su id
                .get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val documentId = documents.documents[0].id
                        // Utilizamos el id del documento para actualizar los datos
                        db.collection("books")
                            .document(documentId)
                            .set(book) // Utilizamos set para actualizar el documento existente
                            .addOnSuccessListener {
                                result.complete(true)
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error updating document", e)
                                result.complete(false)
                            }
                    } else {
                        // El libro con el id dado no existe
                        //  Log.e(TAG, "Book with ID ${book.id} not found")
                        result.complete(false)
                    }
                }
                .addOnFailureListener { exception ->
                    //Log.e(TAG, "Error getting documents", exception)
                    result.complete(false)
                }
        }

        return result
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getSearchBooks(type: String, text: String, callback: (List<BookItem.Book>) -> Unit) {
        val searchBooks = mutableListOf<BookItem.Book>()
        db.collection("books")
            .whereGreaterThanOrEqualTo(type, text)
            .whereLessThanOrEqualTo(type, text + "\uf8ff") // \uf8ff es el último carácter Unicode
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val book = document.toObject(BookItem.Book::class.java)
                    if (book != null) {
                        searchBooks.add(book)
                    }
                }
                callback(searchBooks)
            }
            .addOnFailureListener { exception ->
                callback(emptyList())
            }
    }

    //////////////////
    // Zona usuario //
    /////////////////
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
    fun updateUser(user: User.User): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()
        val db = FirebaseFirestore.getInstance()
        val userDocument = db.collection("users").document(user.document)
        val updates = mutableMapOf<String, Any>()

        user.name?.let { updates["name"] = it }
        user.lastname?.let { updates["lastname"] = it }
        user.imageUrl?.let { updates["imageUrl"] = it }
        user.favorites?.let { updates["favorites"] = it }
        user.sanction?.let { updates["sanction"] = it }

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


    @RequiresApi(Build.VERSION_CODES.N)
    fun getUserByEmail(email: String): CompletableFuture<User.User?> {
        val result = CompletableFuture<User.User?>()
        db.collection("users")
            .whereEqualTo("email", email.lowercase())
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userData = documents.documents[0].toObject(User.User::class.java)
                    result.complete(userData)
                } else {
                    result.complete(null)
                }
            }
            .addOnFailureListener { exception ->
                result.completeExceptionally(exception)
            }

        return result
    }

    ////////////////////////////////////
    // Zona prestamos y devoluciones //
    ///////////////////////////////////

    @RequiresApi(Build.VERSION_CODES.N)
    fun savedLend(lend: Lend.LendBook): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()
        db.collection("lend")
            .add(lend)
            .addOnSuccessListener {
                result.complete(true)
            }
            .addOnFailureListener {
                result.complete(false)
            }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun savedReturn(returnBook: ReturnBook.ReturnBook): CompletableFuture<Boolean> {
        val result = CompletableFuture<Boolean>()
        db.collection("return")
            .add(returnBook)
            .addOnSuccessListener {
                result.complete(true)
            }
            .addOnFailureListener {
                result.complete(false)
            }
        return result
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun getLendBook(id: Int): CompletableFuture<Lend.LendBook?> {
        val result = CompletableFuture<Lend.LendBook?>()
        db.collection("lend")
            .whereEqualTo("bookId", id)
            .orderBy("loanDate", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val lendBook = documents.documents[0].toObject(Lend.LendBook::class.java)
                    result.complete(lendBook)
                } else {
                    result.complete(null)
                }
            }
            .addOnFailureListener { exception ->
                result.completeExceptionally(exception)
            }

        return result
    }
}