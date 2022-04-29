package com.example.myrecipeapplication.profile

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myrecipeapplication.R
import com.example.myrecipeapplication.bottombar.BottomActivityDirections
import com.example.myrecipeapplication.databinding.FragmentEditEmailBinding
import com.example.myrecipeapplication.databinding.FragmentRemoveUserBinding
import com.example.myrecipeapplication.recipe.DetailsRecipeFragmentDirections
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class RemoveUserFragment : Fragment(R.layout.fragment_remove_user) {
    // view binding
    lateinit var binding: FragmentRemoveUserBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    // FirebaseUser
    private lateinit var user: FirebaseUser

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        binding = FragmentRemoveUserBinding.inflate(inflater, container, false)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // toolbar return button
        binding.toolBarMain.setNavigationOnClickListener {
            // navigate back to bottom activity
            val action =
                RemoveUserFragmentDirections.actionRemoveUserFragmentToBottomActivity("EditEmailFragment")
            findNavController().navigate(action)
        }

        // remove user button
        binding.removeUser.setOnClickListener {
            // get credential from user
            val credential = EmailAuthProvider
                .getCredential(
                    binding.editTextEmail.text.toString(),
                    binding.editTextPassword.text.toString()
                )
            // call remove user function
            removeUser(credential, view)
        }
    }

    private fun removeUser(credential: AuthCredential, view: View) {
        // alert dialog
        MaterialAlertDialogBuilder(view.context)
            .setMessage(resources.getString(R.string.remove_account_dialog))
            .setNegativeButton(resources.getString(R.string.cancel_remove)) { dialog, which ->
                // close alert dialog
                dialog.dismiss()
            }
            .setPositiveButton(resources.getString(R.string.confirm_remove)) { dialog, which ->
                // remove user in firebase auth
                user.reauthenticate(credential)
                    .addOnCompleteListener {
                        Log.d(ContentValues.TAG, "User re-authenticated.")
                        user.delete()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(ContentValues.TAG, "User password updated.")
                                }
                                // navigate to home fragment
                                val action = RemoveUserFragmentDirections.actionRemoveUserFragmentToHomeFragment()
                                findNavController().navigate(action)
                            }
                            .addOnFailureListener {
                                Log.d(ContentValues.TAG, "User password update failed.")
                            }
                    }


            }
            .show()
    }

}