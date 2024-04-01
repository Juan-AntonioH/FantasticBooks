package com.juanhegi.fantasticbooks.ui.books

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.juanhegi.fantasticbooks.MainActivity
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.ui.service.FirebaseService
import com.juanhegi.fantasticbooks.ui.user.UserViewModel

class BookFragment : Fragment() {

    private var columnCount = 1
    private val firebaseService = FirebaseService()
    lateinit var user: UserViewModel
    lateinit var mainActivity: MainActivity
    private lateinit var addBook: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        mainActivity = requireActivity() as MainActivity
        addBook = mainActivity.getFab()!!
        addBook.isVisible = user.user?.rol == "librarian"

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_book_list, container, false)
        val genre = arguments?.getString("genero")
        val activity = activity as AppCompatActivity?
        activity!!.supportActionBar!!.title = genre
        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                firebaseService.getBooksByGenre(genre.toString()) { bookList ->
                    adapter = MybookRecyclerViewAdapter(bookList,findNavController())
                }

            }
        }
        addBook.setOnClickListener{
            val bundle = Bundle().apply {
                putString("action", "genre")
                putString("genre", genre)
            }
            findNavController().navigate(R.id.action_bookFragmentList_to_bookSaveUpdate, bundle)
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        addBook.isVisible = false
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            BookFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}