package com.example.myrecipeapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.myrecipeapplication.R
import com.example.myrecipeapplication.data.PostModel
import com.example.myrecipeapplication.databinding.FragmentRecipeCardBinding
import java.util.*


class PostListAdapter(
    var posts: MutableList<PostModel>,
    private val clickListener: (PostModel) -> Unit
) :
    RecyclerView.Adapter<PostListAdapter.PostViewHolder>() {


    inner class PostViewHolder(private val binding: FragmentRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        // binding data
        fun bind(postModel: PostModel, clickListener: (PostModel) -> Unit) {
            // load image
            if (postModel.image.isNotEmpty()) {
                binding.recipeImage.load(postModel.image)
            } else {
                binding.recipeImage.setImageResource(R.drawable.ic_image_not_supported)
            }

            binding.recipeTitle.text = postModel.title
            binding.recipeDesc.text = postModel.description

            // onClickListener on post
            binding.card.setOnClickListener {
                clickListener(postModel)
            }

            binding.recipeCreator.text = "Created by ${postModel.user}"
        }
    }

    // create view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = FragmentRecipeCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PostViewHolder(binding)
    }

    // bind the the items
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        // call bind function
        holder.bind(posts[position], clickListener)
    }

    override fun getItemCount(): Int {
        return posts.size
    }
}