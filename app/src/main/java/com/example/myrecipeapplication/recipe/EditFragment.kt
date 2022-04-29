package com.example.myrecipeapplication.recipe

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.myrecipeapplication.R
import com.example.myrecipeapplication.databinding.FragmentEditRecipeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.util.*

class EditFragment : Fragment(R.layout.fragment_edit_recipe) {
    //ViewBinding
    private lateinit var binding: FragmentEditRecipeBinding

    // args
    private val args: EditFragmentArgs by navArgs()

    // Firestore
    private val db = Firebase.firestore

    // recipe id
    private lateinit var docId: String

    // current fragment
    private lateinit var currentFragment: String

    // image url
    private var imageURL: String = ""

    // firebase storage
    private lateinit var firebaseStorage: FirebaseStorage

    // uploadTask
    private lateinit var uploadTask: UploadTask

    // user uid
    private lateinit var userUid: String

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        binding = FragmentEditRecipeBinding.inflate(inflater, container, false)

        // init firebase storage
        firebaseStorage = FirebaseStorage.getInstance()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        userUid = firebaseAuth.currentUser?.uid.toString()

        getRecipeId()

        return binding.root
    }

    // get recipe document id
    private fun getRecipeId() {
        if (args.public) {
            db.collection("Recipe")
                .whereEqualTo("title", args.title)
                .get()
                .addOnCompleteListener {
                    for (queryDocumentSnapshot in it.result) {
                        docId = queryDocumentSnapshot.id
                    }
                }
        } else {
            db.collection(userUid)
                .whereEqualTo("title", args.title)
                .get()
                .addOnCompleteListener {
                    for (queryDocumentSnapshot in it.result) {
                        docId = queryDocumentSnapshot.id
                    }
                }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setEditTextData()

        // set current fragment
        currentFragment = if (args.public) {
            "Home Recipe"
        } else {
            "myRecipe"
        }

        // upload image button
        binding.recipeImageUpload.setOnClickListener {
            selectImageFromGallery()
            binding.editTextImage.setText("")
        }

        // close button
        binding.closeButton.setOnClickListener {
            // navigate to Welcome fragment
            val action = EditFragmentDirections.actionEditFragmentToBottomActivity(currentFragment)
            findNavController().navigate(action)
        }

        // edit button
        binding.editButton.setOnClickListener {
            val title = binding.editTextTitle.text.toString().trim()
            val image = binding.editTextImage.text.toString().trim()
            val description = binding.editTextDesc.text.toString().trim()
            val type = binding.editTextType.text.toString().trim()
            val details = binding.editTextDetails.text.toString().trim()

            // edit recipe
            if (args.public) {
                db.collection("Recipe").document(docId)
                    .update(
                        "title", title,
                        "description", description,
                        "image", image,
                        "details", details,
                        "type", type,
                    )
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Recipe updated", Snackbar.LENGTH_LONG)
                            .show()

                        val action = EditFragmentDirections.actionEditFragmentToBottomActivity(
                            currentFragment
                        )
                        findNavController().navigate(action)
                    }
                    .addOnFailureListener {
                        Snackbar.make(
                            binding.root,
                            "Update failed, Please try again",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
            } else {
                db.collection(userUid).document(docId)
                    .update(
                        "title", title,
                        "description", description,
                        "image", image,
                        "type", type,
                        "details", details,
                    )
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Recipe updated", Snackbar.LENGTH_LONG)
                            .show()

                        val action = EditFragmentDirections.actionEditFragmentToBottomActivity(
                            currentFragment
                        )
                        findNavController().navigate(action)
                    }
                    .addOnFailureListener {
                        Snackbar.make(
                            binding.root,
                            "Update failed, Please try again",
                            Snackbar.LENGTH_LONG
                        )
                            .show()
                    }
            }

        }

    }

    // set edit text data from args
    private fun setEditTextData() {
        binding.editTextImage.setText(args.image)
        binding.editTextDesc.setText(args.description)
        binding.editTextDetails.setText(args.details)
        binding.editTextTitle.setText(args.title)
        binding.editTextType.setText(args.type)
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
                    Log.d(ContentValues.TAG, imageURL)
                } else {
                    Log.d(ContentValues.TAG, "error")
                }
            }
        }
    }
}