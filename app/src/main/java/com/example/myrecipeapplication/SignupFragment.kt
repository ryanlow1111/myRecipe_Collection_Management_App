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
import com.example.myrecipeapplication.databinding.FragmentSignupBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class SignupFragment : Fragment(R.layout.fragment_signup) {
    //ViewBinding
    private lateinit var binding: FragmentSignupBinding

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    //email and password
    private var email = ""
    private var password = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        //firebase init
        firebaseAuth = FirebaseAuth.getInstance()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //create user
        binding.buttonSignup.setOnClickListener {
            validateData()
            hideKeyboard()
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
        } else if (password.length < 6) {
            //password length less than 6
            binding.textInputPassword.error = getString(R.string.signup_password_error)
            binding.textInputEmail.error = null
        } else {
            //data is validated proceed to login
            firebaseSignup()
        }
    }

    private fun firebaseSignup() {
        val contextView = binding.root
        //it will auto login after sign up, put sign out to make user stay on login fragment
        //firebase auth only create user with email format and password length >6
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                Snackbar.make(contextView, "Account Created", Snackbar.LENGTH_LONG)
                    .show()
                firebaseAuth.signOut()
                val action = SignupFragmentDirections.actionSignupFragmentToLoginFragment()
                findNavController().navigate(action)
            }
            .addOnFailureListener {
                Snackbar.make(contextView, "Sign Up Error, Please try again", Snackbar.LENGTH_LONG)
                    .show()
            }
    }

}