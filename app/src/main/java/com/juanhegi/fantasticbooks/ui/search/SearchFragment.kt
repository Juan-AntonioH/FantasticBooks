package com.juanhegi.fantasticbooks.ui.search

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
import androidx.navigation.fragment.findNavController
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.ui.service.FirebaseService

class SearchFragment : Fragment() {
    private val firebaseService = FirebaseService()
    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_search_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
                val type = arguments?.getString("param1")
                val text = arguments?.getString("param2")
                firebaseService.getSearchBooks(type.toString(), text.toString()) { bookList ->
                    if(bookList.isEmpty()){
                        val alertDialogBuilder = AlertDialog.Builder(requireContext())
                        alertDialogBuilder.setTitle("Libro no encontrado")
                        alertDialogBuilder.setMessage("No hay ningún libro con esos parámetros.")
                        alertDialogBuilder.setPositiveButton("Aceptar") { dialogInterface: DialogInterface, i: Int ->
                            dialogInterface.dismiss()
                        }
                        alertDialogBuilder.show()
                    }else{
                        adapter = MysearchRecyclerViewAdapter(bookList, findNavController())
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
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_COLUMN_COUNT, columnCount)
                }
            }

        fun newInstance2(param1: String, param2: String): SearchFragment {
            val fragment = SearchFragment()
            val args = Bundle()
            args.putString("param1", param1)
            args.putString("param2", param2)
            fragment.arguments = args
            return fragment
        }
    }
}