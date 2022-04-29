package com.example.myrecipeapplication.bottombar

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myrecipeapplication.R
import com.example.myrecipeapplication.adapter.PostListAdapter
import com.example.myrecipeapplication.data.PostModel
import com.example.myrecipeapplication.databinding.FragmentMyrecipeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class MyRecipeFragment : Fragment(R.layout.fragment_myrecipe), (PostModel) -> Unit {

    //FirebaseAuth
    private lateinit var firebaseAuth: FirebaseAuth

    // user uid
    private lateinit var userUid: String

    //viewBinding
    private lateinit var binding: FragmentMyrecipeBinding

    //firebase Firestore
    private lateinit var firebaseFirestore: FirebaseFirestore

    // List
    private var postList = mutableListOf<PostModel>()

    private var tempList = mutableListOf<PostModel>()

    // Adapter
    private val adapter: PostListAdapter = PostListAdapter(tempList, this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout
        binding = FragmentMyrecipeBinding.inflate(inflater, container, false)

        // init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()
        userUid = firebaseAuth.currentUser?.uid.toString()

        //init Firestore
        firebaseFirestore = FirebaseFirestore.getInstance()

        // load Firestore
        loadFirestoreData()

        // init adapter
        binding.recipeList.adapter = adapter
        binding.recipeList.layoutManager = LinearLayoutManager(activity)

        return binding.root
    }

    // load Firestore data and bind into postList
    private fun loadFirestoreData() {
        firebaseFirestore
            .collection(userUid)
            .orderBy("timestamp")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    postList = it.result!!.toObjects(PostModel::class.java)
                    adapter.posts = postList
                    tempList.addAll(postList)
                    adapter.notifyDataSetChanged()

                } else {
                    Log.d(TAG, "Error: ${it.exception!!.message}")
                }
            }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // set tool bar item click listener
        binding.toolBarMain.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.log_out -> {
                    firebaseAuth.signOut()

                    Snackbar.make(binding.root, "Successfully Logout", Snackbar.LENGTH_LONG)
                        .show()

                    val action = BottomActivityDirections.actionBottomActivityToHomeFragment()
                    findNavController().navigate(action)
                    true
                }
                R.id.search_recipe -> {
                    // set menuItem as a SearchView
                    val searchView = menuItem.actionView as SearchView
                    searchData(searchView)
                    true
                }
                else -> false
            }
        }

        // pull up refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            postList.clear()
            loadFirestoreData()
            binding.swipeRefreshLayout.isRefreshing = false
        }

        //respond to FAB click
        binding.floatingActionButton.setOnClickListener {
            //navigate to add recipe fragment
            val action = BottomActivityDirections.actionBottomActivityToAddRecipeFragment()
            findNavController().navigate(action)
        }
    }

    // search recipe by recipe name or recipe type
    private fun searchData(search: SearchView) {
        search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                tempList.clear()
                val searchText = newText!!.lowercase(Locale.getDefault())
                if (searchText.isNotEmpty()) {
                    postList.forEach {
                        if (it.title.lowercase(Locale.getDefault())
                                .contains(searchText) || it.type.lowercase(Locale.getDefault())
                                .contains(searchText)
                        ) {
                            tempList.add(it)
                        }
                    }
                    adapter.posts = tempList
                    adapter.notifyDataSetChanged()
                } else {
                    tempList.clear()
                    tempList.addAll(postList)
                    adapter.posts = tempList
                    adapter.notifyDataSetChanged()
                }
                return true
            }
        })
    }

    // Item click and navigate to detail fragment
    override fun invoke(postModel: PostModel) {
        // navigate to add recipe fragment with image, title and details
        val action = BottomActivityDirections.actionBottomActivityToDetailsRecipeFragment(
            postModel.image,
            postModel.title,
            postModel.details,
            postModel.public
        )
        findNavController().navigate(action)

    }

}