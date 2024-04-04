package com.juanhegi.fantasticbooks.ui.search

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.juanhegi.fantasticbooks.R


class Buscador : Fragment() {
    private lateinit var btnSearch: Button
    private lateinit var searchBook: EditText
    private lateinit var spinner: Spinner

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_buscador, container, false)
        btnSearch = view.findViewById(R.id.btnSearch)
        searchBook = view.findViewById(R.id.searchBook)
        spinner = view.findViewById(R.id.spinner)

        val options = resources.getStringArray(R.array.spinner)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        btnSearch.setOnClickListener {
            val searchText = searchBook.text.toString()
            val searchType = switch(spinner.selectedItem.toString())
            if (searchText.isBlank()){
                val alertDialogBuilder = AlertDialog.Builder(requireContext())
                alertDialogBuilder.setTitle("Error")
                alertDialogBuilder.setMessage("No puedes dejar el campo de búsqueda vacío.")
                alertDialogBuilder.setPositiveButton("Aceptar") { dialogInterface: DialogInterface, i: Int ->
                    dialogInterface.dismiss()
                }
                alertDialogBuilder.show()
            }else{
                val fragment = SearchFragment.newInstance2(searchType, searchText)
                parentFragmentManager.beginTransaction().apply {
                    replace(R.id.fragmentContainerView, fragment)
                    commit()
                }
            }

        }

        return view
    }

    private fun switch(name: String): String {
        return when (name) {
            "Autor" -> "author"
            "Título" -> "title"
            "ISBN" -> "isbn"
            else -> ""
        }
    }
}