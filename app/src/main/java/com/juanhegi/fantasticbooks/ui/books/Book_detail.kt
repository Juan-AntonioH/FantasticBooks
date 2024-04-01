package com.juanhegi.fantasticbooks.ui.books

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.databinding.FragmentBookDetailBinding
import com.juanhegi.fantasticbooks.ui.service.FirebaseService
import com.juanhegi.fantasticbooks.ui.user.UserViewModel
import com.squareup.picasso.Picasso
import android.graphics.Paint
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.EditText
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.juanhegi.fantasticbooks.MainActivity
import com.juanhegi.fantasticbooks.ui.lend_return.Lend
import com.juanhegi.fantasticbooks.ui.lend_return.ReturnBook
import com.juanhegi.fantasticbooks.ui.user.User
import java.util.Calendar


class Book_detail : Fragment() {
    private var menu: Menu? = null
    lateinit var binding: FragmentBookDetailBinding
    private lateinit var book: BookItem.Book
    private val firebaseService = FirebaseService()
    lateinit var user: UserViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        val bookid: Int = arguments?.getInt("book")!!
        firebaseService.getBooksById(bookid) { book ->
            this.book = book!!
            setText()
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var message = ""
        var error = ""
        binding.favoriteBookDetail.setOnClickListener {
            val currentFavorites = user.user?.favorites?.toMutableList() ?: mutableListOf()
            if (currentFavorites.contains(book.id)) {
                currentFavorites.remove(book.id)
                binding.favoriteBookDetail.setImageResource(R.drawable.no_favorite)
                message = "Se quitó el libro de la lista de favoritos."
                error = "Ocurrió un error al quitarlo de la lista de favoritos."
            } else {
                currentFavorites.add(book.id)
                binding.favoriteBookDetail.setImageResource(R.drawable.favorite)
                message = "El libro se guardó en la lista de favoritos."
                error = "Ocurrió un error al guardarlo en la lista de favoritos."
            }
            user.user?.favorites = currentFavorites.toList()

            firebaseService.updateUser(user.user!!)
                .thenAccept { isUserUpdated ->
                    if (isUserUpdated) {
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            error,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        this.menu = menu
        if (user.user?.rol == "librarian") {
            menu.setGroupVisible(0, true)
        } else {
            menu.setGroupVisible(0, false)
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                val bundle = Bundle().apply {
                    putString("action", "book")
                    putInt("book", book.id)
                }
                findNavController().navigate(R.id.action_book_detail_to_bookSaveUpdate, bundle)
                true
            }

            R.id.action_lend -> {
                lend()
                true
            }

            R.id.action_return -> {
                returnBook()
                true
            }

            R.id.action_delete -> {
                deleteBook()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        menu?.setGroupVisible(0, false)
    }

    private fun setText() {
        if (book.available) {
            binding.avaliabeBookDetail.text = "Disponible"
            binding.avaliabeBookDetail.setTextColor(Color.GREEN)
        } else {
            binding.avaliabeBookDetail.text = "No Disponible"
            binding.avaliabeBookDetail.setTextColor(Color.RED)
        }
        binding.authorBookDetail.text = book.author
        binding.dateBookDetail.text = book.datePublication
        binding.numPagesBookDetail.text = book.numPages.toString()
        binding.titleBookDetail.text = book.title
        Picasso.get().load(book.imagenSrc).into(binding.imageBookDetail)
        binding.descriptionBookDetail.text = book.description
        binding.descriptionBookDetail.movementMethod = ScrollingMovementMethod()
        binding.textviewDescription.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        if (user.user == null) {
            binding.textviewFavorites.isVisible = false
            binding.favoriteBookDetail.isVisible = false
        } else {
            if (user.user?.favorites!!.contains(book.id)) {
                binding.favoriteBookDetail.setImageResource(R.drawable.favorite)
            } else {
                binding.favoriteBookDetail.setImageResource(R.drawable.no_favorite)
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun checkUserExistence(email: String) {
        firebaseService.getUserByEmail(email).thenAccept { userData ->
            if (userData != null) {
                if (userData.sanction == null) {
                    lendBook(userData.email)
                } else {
                    val sanctionDate = userData.sanction?.toDate()
                    val now = Timestamp.now().toDate()
                    val differenceMillis = sanctionDate!!.time - now.time
                    val differenceDays = differenceMillis / (1000 * 60 * 60 * 24)

                    if (differenceDays > 0) {
                        Toast.makeText(
                            requireContext(),
                            "El usuario no puede coger libros hasta dentro de $differenceDays días.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        lendBook(userData.email)
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "No existe un usuario con ese correo.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.exceptionally { exception ->
            Toast.makeText(
                requireContext(),
                "Ocurrió un error al obtener el usuario.",
                Toast.LENGTH_SHORT
            ).show()
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun lendBook(email: String) {
        book.available = false
        firebaseService.saveOrUpdateBook(book).thenAccept { isSaved ->
            if (isSaved) {
                val newLend: Lend.LendBook = Lend.LendBook()
                newLend.userEmail = email
                newLend.bookId = book.id
                firebaseService.savedLend(newLend).thenAccept { result ->
                    if (result) {
                        binding.avaliabeBookDetail.text = "No Disponible"
                        binding.avaliabeBookDetail.setTextColor(Color.RED)
                        Toast.makeText(
                            requireContext(),
                            "Se realizo el préstamo correctamente.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Ocurrio un error al crear el registro.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Ocurrio un error al actualizar el estado del libro.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun savedReturnBook(returnBook: ReturnBook.ReturnBook) {
        firebaseService.savedReturn(returnBook).thenAccept { result ->
            if (result) {
                binding.avaliabeBookDetail.text = "Disponible"
                binding.avaliabeBookDetail.setTextColor(Color.GREEN)
                book.available = true
                firebaseService.saveOrUpdateBook(book)
                Toast.makeText(
                    requireContext(),
                    "Se realizó la devolución correctamente.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Ocurrio un error al crear el registro.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun limitReturn(returnBook: ReturnBook.ReturnBook) {
//TODO: Obtener el usuario y aplicarle una sanción, avisar con un alertDialog
        firebaseService.getUserByEmail(returnBook.userEmail).thenAccept { userData ->
            if (userData != null) {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = Timestamp.now().seconds * 1000
                calendar.add(Calendar.DAY_OF_MONTH, 7)
                userData.sanction = Timestamp(calendar.timeInMillis / 1000, 0)
                firebaseService.updateUser(userData).thenAccept { result ->
                    if (result) {
                        val alertDialogBuilder = AlertDialog.Builder(requireContext())
                        alertDialogBuilder.setTitle("Sanción Aplicada")
                        alertDialogBuilder.setMessage("El usuario ha sido sancionado durante una semana.")

                        alertDialogBuilder.setPositiveButton("Aceptar") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        alertDialogBuilder.show()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Ocurrió un error al aplicar la sanción.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.exceptionally { exception ->
            Toast.makeText(
                requireContext(),
                "Ocurrió un error al obtener el usuario.",
                Toast.LENGTH_SHORT
            ).show()
            null
        }
        savedReturnBook(returnBook)
    }

    /////////////// Acciones menú //////////
    @RequiresApi(Build.VERSION_CODES.N)
    private fun deleteBook() {
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle("Eliminar libro")
        alertDialogBuilder.setMessage("¿Estás seguro que quieres eliminar este libro?")

        alertDialogBuilder.setPositiveButton("Sí") { dialogInterface: DialogInterface, i: Int ->
            book.removed = true
            firebaseService.saveOrUpdateBook(book).thenAccept { isUpdated ->
                if (isUpdated) {
                    Toast.makeText(
                        requireContext(),
                        "El libro fue eliminado correctamente.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        val navController = findNavController()
                        navController.popBackStack()
                    }, 500)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ocurrio un error al intentar eliminarlo",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        alertDialogBuilder.setNegativeButton("No") { dialogInterface: DialogInterface, i: Int ->
            dialogInterface.dismiss()
        }
        alertDialogBuilder.show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun lend() {
        if (book.available) {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle("Prestar libro")
            alertDialogBuilder.setMessage("Indique el email del usuario")

            val input = EditText(requireContext())
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            input.layoutParams = lp
            alertDialogBuilder.setView(input)

            alertDialogBuilder.setPositiveButton("Aceptar") { dialogInterface: DialogInterface, i: Int ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty()) {
                    checkUserExistence(email)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Debes ingresar un correo electrónico",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            alertDialogBuilder.setNegativeButton("Cancelar") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            alertDialogBuilder.show()
        } else {
            Toast.makeText(
                requireContext(),
                "El libro no está disponible",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun returnBook() {
        var lendBook: Lend.LendBook = Lend.LendBook()
        if (!book.available) {
            firebaseService.getLendBook(book.id).thenAccept { isGetLend ->
                if (isGetLend != null) {
                    lendBook = isGetLend
                    var returnBook: ReturnBook.ReturnBook = ReturnBook.ReturnBook()
                    returnBook.bookId = book.id
                    returnBook.userEmail = lendBook.userEmail
                    if (lendBook.returnDate < Timestamp.now()) {
                        limitReturn(returnBook)
                    } else {
                        savedReturnBook(returnBook)
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Ocurrió un error al comprobar el prestamo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "El libro ya está en la biblioteca.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}