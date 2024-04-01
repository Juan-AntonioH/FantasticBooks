package com.juanhegi.fantasticbooks.ui.auth

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
import androidx.navigation.fragment.findNavController
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.databinding.FragmentRegisterBinding
import com.juanhegi.fantasticbooks.ui.service.FirebaseService
import com.juanhegi.fantasticbooks.ui.user.User
import java.io.InputStream

class RegisterFragment : Fragment() {
    private val PICK_IMAGE_REQUEST = 1
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    binding.imageRegister.setImageURI(uri)
                    this.uri = uri
                }
            }
        }
    lateinit var loadingDialog: Dialog
    private lateinit var storageReference: StorageReference
    lateinit var binding: FragmentRegisterBinding
    val authFunction: AuthFunction = AuthFunction()
    var imageUrl: String? = null
    private var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageReference = FirebaseStorage.getInstance().reference
        loadingDialog = createLoadingDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnRegister.setOnClickListener {
            val errors = StringBuilder()
            val emailError = authFunction.isValidEmail(binding.emailRegister.text.toString())
            val passwordError =
                authFunction.isValidPassword(binding.passwordRegister.text.toString())
            val repeatPasswordError =
                authFunction.isValidRepeatPassword(binding.passwordRegister.text.toString(),
                    binding.repeatPasswordRegister.text.toString())
            val name = binding.nameRegister.text.toString()
            val lastname = binding.lastnameRegister.text.toString()

            if (emailError.isNotEmpty()) {
                errors.append("\n$emailError")
            }
            if (name.isEmpty()) {
                errors.append("\nDebes poner el nombre")
            }
            if (lastname.isEmpty()) {
                errors.append("\nDebes poner el apellido")
            }
            if (passwordError.isNotEmpty()) {
                errors.append("\n$passwordError")
            }
            if (repeatPasswordError.isNotEmpty()) {
                errors.append("\n$repeatPasswordError")
            }

            if (errors.isBlank()) {

                loadingDialog.show()
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        binding.emailRegister.text.toString(),
                        binding.passwordRegister.text.toString()
                    ).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Obtener el ID de usuario recién registrado
                            val userId = task.result?.user?.uid
                            if (userId != null) {
                                if (this.uri != null){
                                    saveImageToFirebase(this.uri!!, userId)
                                }else{
                                    saveUserToFirestore(userId,null)
                                }
                            }
                        }else{
                            loadingDialog.dismiss()
                            showAlert("Se ha producido un error al registrar al usuario")
                        }
                    }

            }else{
                loadingDialog.dismiss()
                showAlert(errors.toString())
            }
        }
        binding.btnImage.setOnClickListener {
            // Crea un intent para seleccionar una imagen de la galería
            val intent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveImageToFirebase(imageUri: Uri, userId: String) {

            val imageName = "$userId.jpg"
            val imagesRef = storageReference.child("images/$imageName")

            val inputStream: InputStream? = context?.contentResolver?.openInputStream(imageUri)
            inputStream?.let {
                imagesRef.putStream(it)
                    .addOnSuccessListener { taskSnapshot ->
                        imagesRef.downloadUrl.addOnSuccessListener { uri ->
                            imageUrl = uri.toString()
                            saveUserToFirestore(userId,imageUrl)
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
    private fun saveUserToFirestore( userId: String,imageUrl: String?) {
        val newUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            User.User(
                document = userId,
                name = binding.nameRegister.text.toString(),
                lastname = binding.lastnameRegister.text.toString(),
                email = binding.emailRegister.text.toString().lowercase(),
                imageUrl = imageUrl,
                sanction = null,
                dischargeDate = Timestamp.now(), // Fecha actual
                rol = "user"
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val firebaseService = FirebaseService()
        firebaseService.saveUser(newUser, userId)
            .thenAccept { isUserSaved ->
                if (isUserSaved) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Usuario registrado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().popBackStack(R.id.nav_login, false)
                    }, 500)
                } else {
                    loadingDialog.dismiss()
                    showAlert("Se ha producido un error al registrar al usuario")
                }
            }
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