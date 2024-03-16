package com.juanhegi.fantasticbooks.ui.auth

import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.ui.service.FirebaseService
import com.juanhegi.fantasticbooks.ui.user.User
import com.juanhegi.fantasticbooks.ui.user.UserViewModel

class LoginFragment : Fragment() {
    lateinit var loadingDialog: Dialog
    lateinit var user: UserViewModel
    lateinit var mtextMail: EditText
    lateinit var mtextPassword: EditText
    lateinit var mforgetPassword: TextView
    lateinit var mregister: TextView
    lateinit var mbtnLogin: Button
    val authFunction: AuthFunction = AuthFunction()
    private val firebaseService = FirebaseService()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = createLoadingDialog()
        user = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        Toast.makeText(
            requireContext(),
            user.user?.name.toString(),
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        mtextMail = view.findViewById(R.id.loginEmail)
        mtextPassword = view.findViewById(R.id.loginPassword)
        mforgetPassword = view.findViewById(R.id.txtForgetPassword)
        mregister = view.findViewById(R.id.txtRegister)
        mbtnLogin = view.findViewById(R.id.btnLogin)


        return view
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mbtnLogin.setOnClickListener {
            val email = mtextMail.text.toString()
            val password = mtextPassword.text.toString()
            val errors = StringBuilder()

            val emailError = authFunction.isValidEmail(email)
            if (emailError.isNotEmpty()) {
                errors.append("\n$emailError")
            }

            if (password.isEmpty()) {
                errors.append("\nEl campo contraseña esta vacio.")
            }

            if (errors.isBlank()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            loadingDialog.show()
                            // Inicio de sesión exitoso
                            val userAuth = FirebaseAuth.getInstance().currentUser
                            userAuth?.let { currentUser ->
                                val userId = currentUser.uid
                                firebaseService.getUserData(userId).thenAccept { userData ->
                                    if (userData != null) {
                                        // Los datos del usuario se obtuvieron correctamente
                                        user.user = userData
                                        loadingDialog.dismiss()
                                        Toast.makeText(
                                            requireContext(),
                                            "Inicio de sesión correcto.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            findNavController().popBackStack(R.id.nav_home, false)
                                        }, 500)
                                    } else {
                                        loadingDialog.dismiss()
                                        showAlert("En la base de datos no existe ese usuario, si el error persiste consulte al administrador.")
                                    }
                                }

                            }
                        } else {
                            showAlert("Error al autenticar el usuario")
                        }
                    }
            } else {
                showAlert(errors.toString())
            }
        }

        mregister.setOnClickListener {
            findNavController().navigate(R.id.action_nav_login_to_registerFragment)
        }

        mforgetPassword.setOnClickListener {
            findNavController().navigate(R.id.action_nav_login_to_passwordRestoreFragment)
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
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_loading, null)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.progressBar)
        val textViewMessage = dialogView.findViewById<TextView>(R.id.textViewMessage)

        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)

        return builder.create()
    }
}