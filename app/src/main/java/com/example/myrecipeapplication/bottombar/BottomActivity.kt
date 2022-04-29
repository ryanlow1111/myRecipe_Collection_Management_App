package com.example.myrecipeapplication.bottombar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.myrecipeapplication.R
import com.example.myrecipeapplication.databinding.ActivityBottomBinding
import com.example.myrecipeapplication.recipe.DetailsRecipeFragmentArgs


class BottomActivity : Fragment(R.layout.activity_bottom) {

    // Args
    private val args: BottomActivityArgs by navArgs()

    //viewBinding
    private lateinit var binding: ActivityBottomBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        binding = ActivityBottomBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val homeFragment = WelcomeFragment()
        val myRecipeFragment = MyRecipeFragment()
        val profileFragment = ProfileFragment()

        // init
        when (args.from) {
            "HomeFragment", "LoginFragment", "Home Recipe" -> setCurrentFragment(homeFragment)
            "myRecipe" -> {
                setCurrentFragment(myRecipeFragment)
                binding.bottomNavigationView.selectedItemId = R.id.my_recipe
            }
            "EditEmailFragment", "EditPasswordFragment" -> {
                setCurrentFragment(profileFragment)
                binding.bottomNavigationView.selectedItemId = R.id.user_profile
            }
        }

        // bottom navigation
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.home_recipe -> {
                    // Respond to navigation item 1 click
                    setCurrentFragment(homeFragment)
                    true
                }
                R.id.my_recipe -> {
                    // Respond to navigation item 2 click
                    setCurrentFragment(myRecipeFragment)
                    true
                }
                R.id.user_profile -> {
                    setCurrentFragment(profileFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setCurrentFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.frame_layout, fragment)
            commit()
        }
    }
}