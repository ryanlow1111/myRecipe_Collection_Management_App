package com.example.myrecipeapplication.bottombar

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myrecipeapplication.R
import com.example.myrecipeapplication.databinding.FragmentProfileBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.ktx.Firebase


class ProfileFragment : Fragment(R.layout.fragment_profile) {
    // viewBinding
    lateinit var binding: FragmentProfileBinding

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
        binding = FragmentProfileBinding.inflate(inflater, container, false)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        user = firebaseAuth.currentUser!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userEmail = user.email.toString()

        // display user current email
        binding.userEmail.text = userEmail

        // get credential from user
        val credential = EmailAuthProvider
            .getCredential(userEmail, "123456")

        // button navigate to edit email
        binding.editEmail.setOnClickListener {
            val action = BottomActivityDirections.actionBottomActivityToEditEmail()
            findNavController().navigate(action)
        }

        // button navigate to edit password
        binding.editPassword.setOnClickListener {
            val action = BottomActivityDirections.actionBottomActivityToEditPasswordFragment()
            findNavController().navigate(action)
        }

        // navigate to remove user fragment
        binding.removeUser.setOnClickListener {
            val action = BottomActivityDirections.actionBottomActivityToRemoveUserFragment()
            findNavController().navigate(action)
        }
    }

    // delete user
    private fun deleteUser(credential: AuthCredential) {
        user.reauthenticate(credential)
            .addOnCompleteListener {
                Log.d(TAG, "User re-authenticated.")
                user.delete()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "User password updated.")
                        }
                    }
                    .addOnFailureListener {
                        Log.d(TAG, "User password update failed.")
                    }
            }
    }
}