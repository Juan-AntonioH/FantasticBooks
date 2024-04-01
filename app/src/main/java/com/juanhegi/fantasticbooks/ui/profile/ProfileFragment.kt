package com.juanhegi.fantasticbooks.ui.profile

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
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.Timestamp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.juanhegi.fantasticbooks.MainActivity
import com.juanhegi.fantasticbooks.R
import com.juanhegi.fantasticbooks.databinding.FragmentProfileBinding
import com.juanhegi.fantasticbooks.ui.auth.AuthFunction
import com.juanhegi.fantasticbooks.ui.service.FirebaseService
import com.juanhegi.fantasticbooks.ui.user.User
import com.juanhegi.fantasticbooks.ui.user.UserViewModel
import com.squareup.picasso.Picasso
import java.io.InputStream

class ProfileFragment : Fragment() {
    private val PICK_IMAGE_REQUEST = 1
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val selectedImageUri = result.data?.data
                selectedImageUri?.let { uri ->
                    binding.imageProfile.setImageURI(uri)
                    this.uri = uri
                }
            }
        }

    lateinit var binding: FragmentProfileBinding
    var imageUrl: String? = null
    private var uri: Uri? = null
    lateinit var loadingDialog: Dialog
    lateinit var user: UserViewModel
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        user = ViewModelProvider(requireActivity()).get(UserViewModel::class.java)
        storageReference = FirebaseStorage.getInstance().reference
        loadingDialog = createLoadingDialog()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageUrl = user.user?.imageUrl
        if (imageUrl == null){
            binding.imageProfile.setImageResource(R.drawable.icon_account)
        }else{
            Picasso.get().load(imageUrl).into(binding.imageProfile)
        }
        val nameProfile = view.findViewById<EditText>(R.id.nameProfile)
        nameProfile.setText(user.user?.name)
        view.findViewById<EditText>(R.id.lastnameProfile).setText(user.user?.lastname)

        binding.btnSaveProfile.setOnClickListener{
            val errors = StringBuilder()
            val name = binding.nameProfile.text.toString()
            val lastname = binding.lastnameProfile.text.toString()

            if (name.isEmpty()) {
                errors.append("\nDebes poner el nombre")
            }
            if (lastname.isEmpty()) {
                errors.append("\nDebes poner el apellido")
            }

            if (errors.isBlank()) {
                loadingDialog.show()
                user.user?.name = name
                user.user?.lastname = lastname
                if (this.uri != null){
                    saveImageToFirebase(this.uri!!, user.user!!.document)
                }else{
                    updateUserToFirestore()
                }
            }else{
                loadingDialog.dismiss()
                showAlert(errors.toString())
            }

        }

        binding.btnImageProfile.setOnClickListener{
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

    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveImageToFirebase(imageUri: Uri, userId: String) {

        val imageName = "$userId.jpg"
        val imagesRef = storageReference.child("images/$imageName")

        val inputStream: InputStream? = context?.contentResolver?.openInputStream(imageUri)
        inputStream?.let {
            imagesRef.putStream(it)
                .addOnSuccessListener { taskSnapshot ->
                    imagesRef.downloadUrl.addOnSuccessListener { uri ->
                        user.user?.imageUrl = uri.toString()
                        updateUserToFirestore()
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
    private fun updateUserToFirestore() {
        val firebaseService = FirebaseService()
        firebaseService.updateUser(user.user!!)
            .thenAccept { isUserUpdated ->
                if (isUserUpdated) {
                    loadingDialog.dismiss()
                    Toast.makeText(
                        requireContext(),
                        "Usuario actualizado correctamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    val mainActivity = requireActivity() as MainActivity
                    mainActivity.update()
                    Handler(Looper.getMainLooper()).postDelayed({
                        findNavController().popBackStack(R.id.nav_home, false)
                    }, 500)
                } else {
                    loadingDialog.dismiss()
                    showAlert("Se ha producido un error al actualizar los datos")
                }
            }
    }
}