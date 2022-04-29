package com.example.myrecipeapplication.recipe

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myrecipeapplication.R
import com.example.myrecipeapplication.databinding.FragmentAddNewRecipeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*
import kotlin.collections.HashMap


class AddRecipeFragment : Fragment(R.layout.fragment_add_new_recipe) {
    //ViewBinding
    private lateinit var binding: FragmentAddNewRecipeBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    // user email
    private lateinit var firebaseUser: String

    // image url
    private var imageURL: String = ""

    // firebase storage
    private lateinit var firebaseStorage: FirebaseStorage

    // uploadTask
    private lateinit var uploadTask: UploadTask

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        binding = FragmentAddNewRecipeBinding.inflate(inflater, container, false)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser?.email.toString()

        // init firebase storage
        firebaseStorage = FirebaseStorage.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = Firebase.firestore

        val dateNow = Calendar.getInstance().time

        // user uid
        val userId = firebaseAuth.currentUser?.uid.toString()

        // upload image button
        binding.recipeImageUpload.setOnClickListener {
            selectImageFromGallery()
        }

        // save button
        binding.saveButton.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val image = binding.editTextImage.text.toString().trim()
            val description = binding.editTextDesc.text.toString().trim()
            val details = binding.editTextDetails.text.toString().trim()
            val type = binding.editTextType.text.toString().trim()

            // set current fragment
            val fromWhere = if (binding.checkToPublic.isChecked) {
                "Home Recipe"
            } else {
                "myRecipe"
            }

            // create new recipe
            val recipe: HashMap<String, Any>
            if (binding.checkToPublic.isChecked) {
                recipe = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "image" to image,
                    "details" to details,
                    "type" to type,
                    "public" to true,
                    "user" to firebaseUser,
                    "timestamp" to dateNow
                )
            } else {
                recipe = hashMapOf(
                    "title" to title,
                    "description" to description,
                    "image" to image,
                    "details" to details,
                    "type" to type,
                    "public" to false,
                    "user" to firebaseUser,
                    "timestamp" to dateNow
                )
            }

            // add recipe to Firestore
            if (binding.checkToPublic.isChecked) {
                db.collection("Recipe")
                    .add(recipe)
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Recipe added", Snackbar.LENGTH_LONG)
                            .show()

                        val action =
                            AddRecipeFragmentDirections.actionAddRecipeFragmentToBottomActivity(
                                fromWhere
                            )
                        findNavController().navigate(action)
                    }
                    .addOnFailureListener {
                        Snackbar.make(
                            binding.root,
                            "Failed to add recipe, Please try again",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
            } else {
                db.collection(userId)
                    .add(recipe)
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Recipe added", Snackbar.LENGTH_LONG)
                            .show()

                        val action =
                            AddRecipeFragmentDirections.actionAddRecipeFragmentToBottomActivity(
                                fromWhere
                            )
                        findNavController().navigate(action)
                    }
                    .addOnFailureListener {
                        Snackbar.make(
                            binding.root,
                            "Failed to add recipe, Please try again",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
            }
        }

        // close button
        binding.closeButton.setOnClickListener {
            var currentFragment = ""
            currentFragment = if (binding.checkToPublic.isChecked) {
                "Home Recipe"
            } else {
                "myRecipe"
            }
            // navigate to welcome fragment
            val action =
                AddRecipeFragmentDirections.actionAddRecipeFragmentToBottomActivity(currentFragment)
            findNavController().navigate(action)
        }

    }

    // select image from device gallery
    private fun selectImageFromGallery() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Please select..."
            ),
            100
        )

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null
            && data.data != null
        ) {
            val fileUri = data.data!!
            uploadImage(fileUri)
        }
    }

    // upload image to firebase and get image url
    private fun uploadImage(fileUri: Uri) {
        val fileName = UUID.randomUUID().toString() + ".jpg"

        val refStorage = firebaseStorage.reference.child("images/$fileName")

        uploadTask = refStorage.putFile(fileUri)

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            refStorage.downloadUrl.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    imageURL = downloadUrl.toString()
                    binding.editTextImage.setText(imageURL)
                } else {
                    Log.d(TAG, "error")
                }
            }
        }
    }
}
