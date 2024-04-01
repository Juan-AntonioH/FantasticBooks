package com.juanhegi.fantasticbooks.ui.favorites

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.ui.service.FirebaseService
import com.juanhegi.fantasticbooks.ui.user.UserViewModel

class favoriteFragment : Fragment() {
    private val firebaseService = FirebaseService()
    lateinit var user: UserViewModel
    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = ViewModelProvider(requireActivity())[UserViewModel::class.java]
        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                firebaseService.getFavoriteBooks(user.user?.favorites){ bookList ->
                    if(bookList.isNotEmpty()){
                        adapter = MyfavoriteRecyclerViewAdapter(bookList, findNavController())
                    }else{
                        val alertDialogBuilder = AlertDialog.Builder(requireContext())
                        alertDialogBuilder.setTitle("Lista favoritos")
                        alertDialogBuilder.setMessage("La lista de favoritos esta vacía.")

                        alertDialogBuilder.setPositiveButton("Aceptar") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        alertDialogBuilder.show()
                    }
                }
            }
        }
        return view
    }

    companion object {
        const val ARG_COLUMN_COUNT = "column-count"

        @JvmStatic
        fun newInstance(columnCount: Int) =
            favoriteFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }
    }
}