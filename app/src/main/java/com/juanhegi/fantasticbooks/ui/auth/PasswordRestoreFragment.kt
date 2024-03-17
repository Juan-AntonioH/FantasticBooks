package com.juanhegi.fantasticbooks.ui.auth

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.databinding.FragmentPasswordRestoreBinding

class PasswordRestoreFragment : Fragment() {
    lateinit var binding: FragmentPasswordRestoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPasswordRestoreBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btnRestorePassword.setOnClickListener{
            val authFunction = AuthFunction()
            if (authFunction.isValidEmail(binding.emailRestorePassword.text.toString()).isBlank()) {
                Firebase.auth.sendPasswordResetEmail(binding.emailRestorePassword.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            showAlert("Se envió un Email para restaurar la contraseña")
                            Handler(Looper.getMainLooper()).postDelayed({
                                findNavController().popBackStack(R.id.nav_login, false)
                            }, 500)
                        }else{
                            showAlert("Ocurrio un error al enviar el Email")
                        }
                    }
            }else{
                showAlert("Debes introducir un Email valido")
            }
        }
    }

    private fun showAlert(errors: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Información")
        builder.setMessage(errors)
        builder.setPositiveButton("Accept", null)
        var dialog: AlertDialog = builder.create()
        dialog.show()
    }

}