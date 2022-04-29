package com.example.myrecipeapplication.recipe

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.example.myrecipeapplication.R
import com.example.myrecipeapplication.bottombar.BottomActivityDirections
import com.example.myrecipeapplication.data.PostModel
import com.example.myrecipeapplication.databinding.FragmentDetailsRecipeBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DetailsRecipeFragment : Fragment(R.layout.fragment_details_recipe) {

    // args
    private val args: DetailsRecipeFragmentArgs by navArgs()

    // viewBinding
    private lateinit var binding: FragmentDetailsRecipeBinding

    //firebase Firestore
    private lateinit var firebaseFirestore: FirebaseFirestore

    // recipe id
    private lateinit var docId: String

    // List
    private var postList = mutableListOf<PostModel>()

    // current fragment
    private lateinit var currentFragment: String

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    // user email
    private lateinit var firebaseUser: String

    // user uid
    private lateinit var userUid: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsRecipeBinding.inflate(inflater, container, false)

        //init Firestore
        firebaseFirestore = FirebaseFirestore.getInstance()

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser?.email.toString()
        userUid = firebaseAuth.currentUser?.uid.toString()

        // set current fragment
        currentFragment = if (args.public) {
            "Home Recipe"
        } else {
            "myRecipe"
        }

        loadData()

        getRecipeId()

        return binding.root
    }

    // get recipe data
    private fun loadData() {
        if (args.public) {
            firebaseFirestore
                .collection("Recipe")
                .whereEqualTo("title", args.title)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        postList = it.result!!.toObjects(PostModel::class.java)
                    } else {
                        Log.d(ContentValues.TAG, "Error: ${it.exception!!.message}")
                    }
                }
        } else {
            firebaseFirestore
                .collection(userUid)
                .whereEqualTo("title", args.title)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        postList = it.result!!.toObjects(PostModel::class.java)
                    } else {
                        Log.d(ContentValues.TAG, "Error: ${it.exception!!.message}")
                    }
                }
        }
    }

    // get recipe document id with title
    private fun getRecipeId() {
        if (args.public) {
            firebaseFirestore
                .collection("Recipe")
                .whereEqualTo("title", args.title)
                .get()
                .addOnCompleteListener {
                    for (queryDocumentSnapshot in it.result) {
                        docId = queryDocumentSnapshot.id
                    }
                }
        } else {
            firebaseFirestore
                .collection(userUid)
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

        // toolbar return button
        binding.toolBarMain.setNavigationOnClickListener {
            // navigate back to welcome fragment
            val action =
                DetailsRecipeFragmentDirections.actionDetailsRecipeFragmentToBottomActivity(
                    currentFragment
                )
            findNavController().navigate(action)
        }

        // set menu share to visible if recipe list from myRecipe
        if (currentFragment == "myRecipe") {
            binding.toolBarMain.menu.findItem(R.id.menu_share).isVisible = true
        }

        // recipe title
        binding.recipeTitle.text = args.title

        // recipe details
        if (args.details == "") {
            binding.recipeDetails.text = "No details added"
        } else {
            binding.recipeDetails.text = args.details
        }

        // load image with strings
        if (args.image == "") {
            binding.imageViewRecipe.setImageResource(R.drawable.ic_image_not_supported)
            binding.imageViewRecipe.scaleType = ImageView.ScaleType.FIT_CENTER
        } else {
            binding.imageViewRecipe.load(args.image)
        }

        //make tool bar item works
        binding.toolBarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_remove -> {
                    MaterialAlertDialogBuilder(view.context)
                        .setMessage(resources.getString(R.string.recipe_remove_dialog))
                        .setNegativeButton(resources.getString(R.string.cancel_remove)) { dialog, which ->
                            // close alert dialog
                            dialog.dismiss()
                        }
                        .setPositiveButton(resources.getString(R.string.confirm_remove)) { dialog, which ->
                            // call delete recipe
                            deleteRecipe()
                        }
                        .show()
                    // call delete recipe
                    true
                }
                R.id.menu_edit -> {
                    // navigate to edit fragment
                    val action =
                        DetailsRecipeFragmentDirections.actionDetailsRecipeFragmentToEditFragment(
                            postList[0].image,
                            postList[0].title,
                            postList[0].details,
                            postList[0].description,
                            postList[0].public,
                            postList[0].type
                        )
                    findNavController().navigate(action)
                    true
                }
                R.id.menu_share -> {
                    // share recipe from myRecipe
                    val dateNow = Calendar.getInstance().time

                    val recipe = hashMapOf(
                        "title" to postList[0].title,
                        "description" to postList[0].description,
                        "image" to postList[0].image,
                        "details" to postList[0].details,
                        "type" to postList[0].type,
                        "public" to true,
                        "user" to firebaseUser,
                        "timestamp" to dateNow
                    )

                    firebaseFirestore.collection("Recipe")
                        .add(recipe)
                        .addOnSuccessListener {
                            Toast.makeText(activity, "Share Success", Toast.LENGTH_SHORT).show()

                            // navigate tp bottom activity
                            val action =
                                DetailsRecipeFragmentDirections.actionDetailsRecipeFragmentToBottomActivity(
                                    currentFragment
                                )
                            findNavController().navigate(action)
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                activity,
                                "Share failed, please try again",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    true
                }
                else -> false
            }
        }

    }

    // Delete recipe from Firestore
    private fun deleteRecipe() {
        if (args.public) {
            firebaseFirestore.collection("Recipe")
                .document(docId)
                .delete()
                .addOnSuccessListener {
                    // navigate back to welcome fragment
                    val action =
                        DetailsRecipeFragmentDirections.actionDetailsRecipeFragmentToBottomActivity(
                            currentFragment
                        )
                    findNavController().navigate(action)
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Remove failed, Please try again", Toast.LENGTH_LONG)
                        .show()
                }
        } else {
            firebaseFirestore.collection(userUid)
                .document(docId)
                .delete()
                .addOnSuccessListener {
                    // navigate back to welcome fragment
                    val action =
                        DetailsRecipeFragmentDirections.actionDetailsRecipeFragmentToBottomActivity(
                            currentFragment
                        )
                    findNavController().navigate(action)
                }
                .addOnFailureListener {
                    Toast.makeText(activity, "Remove failed, Please try again", Toast.LENGTH_LONG)
                        .show()
                }
        }

    }

}