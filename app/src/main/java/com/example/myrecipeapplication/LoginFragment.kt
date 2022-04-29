package com.example.myrecipeapplication

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.myrecipeapplication.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment(R.layout.fragment_login) {
    //ViewBinding
    private lateinit var binding: FragmentLoginBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    //username and password
    private var email = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        binding = FragmentLoginBinding.inflate(inflater, container, false)

        //firebase init
        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //login
        binding.buttonLogin.setOnClickListener {
            validateData()
            hideKeyboard()
        }

        binding.textViewSignup.setOnClickListener {
            //navigate to sign up fragment
            val action = LoginFragmentDirections.actionLoginFragmentToSignupFragment()
            findNavController().navigate(action)
        }

    }

    // close soft keyboard
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    //navigate to welcome fragment if logged in
    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            val action =
                LoginFragmentDirections.actionLoginFragmentToBottomActivity("LoginFragment")
            findNavController().navigate(action)
        }
    }

    private fun validateData() {
        //get username and password value from editText in fragment_login
        email = binding.editTextEmail.text.toString().trim()
        password = binding.editTextPassword.text.toString().trim()

        //validate data
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            //invalid email format
            binding.textInputEmail.error = getString(R.string.email_error)
            binding.textInputPassword.error = null
        } else if (TextUtils.isEmpty(password)) {
            //password empty
            binding.textInputPassword.error = getString(R.string.password_error)
            binding.textInputEmail.error = null
        } else {
            //data is validated proceed to login
            firebaseLogin()
        }
    }

    private fun firebaseLogin() {
        val contextView = binding.root
        //firebase auth login
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Snackbar.make(contextView, "Successfully Login", Snackbar.LENGTH_LONG)
                    .show()
                //navigate to welcome fragment
                val action = LoginFragmentDirections.actionLoginFragmentToBottomActivity("LoginFragment")
                findNavController().navigate(action)
            }
            .addOnFailureListener {
                Snackbar.make(contextView, "Login Error, Please try again", Snackbar.LENGTH_LONG)
                    .show()
            }
    }
}

