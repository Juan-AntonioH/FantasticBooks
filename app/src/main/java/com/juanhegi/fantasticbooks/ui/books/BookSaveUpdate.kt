package com.juanhegi.fantasticbooks.ui.books

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.databinding.FragmentBookSaveUpdateBinding
import com.juanhegi.fantasticbooks.ui.service.FirebaseService
import com.squareup.picasso.Picasso
import java.io.InputStream

class BookSaveUpdate : Fragment() {
    private val PICK_IMAGE_REQUEST = 1
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    binding.imageBookSaveUpdate.setImageURI(uri)
                    this.uri = uri
                }
            }
        }
    lateinit var genre: String
    lateinit var binding: FragmentBookSaveUpdateBinding
    private lateinit var book: BookItem.Book
    private val firebaseService = FirebaseService()
    lateinit var loadingDialog: Dialog
    private var uri: Uri? = null
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageReference = FirebaseStorage.getInstance().reference
        loadingDialog = createLoadingDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookSaveUpdateBinding.inflate(inflater, container, false)
        val action = arguments?.getString("action")
        val activity = activity as AppCompatActivity?
        when (action) {
            "genre" -> {
                genre = arguments?.getString("genre")!!
                binding.bookSaveUpdateGenre.setText(genre)
                binding.btnBookSaveUpdate.text = "Guardar"
                book = BookItem.Book(0, "", "", genre, "", "", "", "", 0, "", true, false)
                activity!!.supportActionBar!!.title = "Agregar libro"
            }

            "book" -> {
                val bookid: Int = arguments?.getInt("book")!!
                firebaseService.getBooksById(bookid) { book ->
                    this.book = book!!
                    setInputs()
                }
                activity!!.supportActionBar!!.title = "Actualizar libro"
            }
        }
        return binding.root
    }

    fun setInputs() {
        binding.bookSaveUpdateGenre.setText(book.genre)
        binding.bookSaveUpdateAuthor.setText(book.author)
        binding.bookSaveUpdateTitle.setText(book.title)
        binding.bookSaveUpdateDescription.setText(book.description)
        binding.bookSaveUpdateISBN.setText(book.isbn)
        binding.bookSaveUpdatePublisher.setText(book.publisher)
        binding.bookSaveUpdatePages.setText(book.numPages.toString())
        binding.bookSaveUpdateDatePublication.setText(book.datePublication)
        Picasso.get().load(book.imagenSrc).into(binding.imageBookSaveUpdate)
        binding.btnBookSaveUpdate.text = "Actualizar"
    }

    fun setBook() {
        book.numPages = binding.bookSaveUpdatePages.text.toString().toInt()
        book.genre = binding.bookSaveUpdateGenre.text.toString()
        book.datePublication = binding.bookSaveUpdateDatePublication.text.toString()
        book.isbn = binding.bookSaveUpdateISBN.text.toString()
        book.title = binding.bookSaveUpdateTitle.text.toString()
        book.author = binding.bookSaveUpdateAuthor.text.toString()
        book.publisher = binding.bookSaveUpdatePublisher.text.toString()
        book.description = binding.bookSaveUpdateDescription.text.toString()

    }

    fun checkInputs(): String {
        val result = StringBuilder()
        if (binding.bookSaveUpdateGenre.text.isBlank()) {
            result.append("\nDebes poner el genero")
        }
        if (binding.bookSaveUpdateAuthor.text.isBlank()) {
            result.append("\nDebes poner el author")
        }
        if (binding.bookSaveUpdateTitle.text.isBlank()) {
            result.append("\nDebes poner el título")
        }
        if (binding.bookSaveUpdateDescription.text.isBlank()) {
            result.append("\nDebes poner algo en la descripción")
        }
        if (binding.bookSaveUpdateISBN.text.isBlank()) {
            result.append("\nDebes poner el ISBN")
        }
        if (binding.bookSaveUpdatePublisher.text.isBlank()) {
            result.append("\nDebes poner el editor")
        }
        if (binding.bookSaveUpdatePages.text.isBlank()) {
            result.append("\nDebes poner el número de páginas")
        }
        if (binding.bookSaveUpdateDatePublication.text.isBlank()) {
            result.append("\nDebes poner la fecha de publicación")
        }
        return result.toString()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnBookSaveUpdate.setOnClickListener {
            val errors = checkInputs()
            if (errors.isBlank()) {
                loadingDialog.show()
                if (this.uri != null) {
                    val name = binding.bookSaveUpdateISBN.text.toString()
                    saveImageToFirebase(this.uri!!, name)
                } else {
                    saveUpdateBook()
                }
            } else {
                loadingDialog.dismiss()
                showAlert(errors)
            }
        }
        binding.btnImageBookSaveUpdate.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveImageToFirebase(imageUri: Uri, name: String) {
        val imageName = "$name.jpg"
        val imagesRef = storageReference.child("images/$imageName")
        val inputStream: InputStream? = context?.contentResolver?.openInputStream(imageUri)
        inputStream?.let {
            imagesRef.putStream(it)
                .addOnSuccessListener { taskSnapshot ->
                    imagesRef.downloadUrl.addOnSuccessListener { uri ->
                        book.imagenSrc = uri.toString()
                        saveUpdateBook()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Error al guardar la imagen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun saveUpdateBook() {
        var message = if (book.id == 0) {
            "Se guardo correctamente el libro"
        } else {
            "Se actualizo correctamente el libro"
        }
        setBook()
        firebaseService.saveOrUpdateBook(book).thenAccept { isUpdated ->
            if (isUpdated) {
                loadingDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    message,
                    Toast.LENGTH_SHORT
                ).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    val navController = findNavController()
                    navController.popBackStack()
                }, 500)
            } else {
                loadingDialog.dismiss()
                showAlert("Se ha producido un error al actualizar los datos")
            }
        }
    }

    private fun showAlert(errors: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Error")
        builder.setMessage("Se ha producido los siguientes errores:$errors")
        builder.setPositiveButton("Accept", null)
        var dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun createLoadingDialog(): AlertDialog {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val textViewMessage = dialogView.findViewById<TextView>(R.id.textViewMessage)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        return builder.create()
    }

}