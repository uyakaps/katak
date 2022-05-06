package com.rasenyer.socialnetwork.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.databinding.ItemSavedBinding
import com.rasenyer.socialnetwork.models.Post

class SavedAdapter: RecyclerView.Adapter<SavedAdapter.MySavedViewHolder>() {

    private var posts: List<Post> = ArrayList()
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var onImageClick: OnImageClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MySavedViewHolder {
        return MySavedViewHolder(ItemSavedBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MySavedViewHolder, position: Int) {

        val post = posts[position]
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        holder.binding.mImagePost.load(post.image) {
            placeholder(R.color.purple_200)
            error(R.color.purple_200)
            crossfade(true)
            crossfade(400)
        }

        holder.binding.mImagePost.setOnClickListener {
            onImageClick.onClick(post)
        }

        holder.binding.mSave.setOnClickListener {

            if (holder.binding.mSave.tag == "Save") {

                FirebaseDatabase.getInstance().reference.child("Saved").child(firebaseUser.uid).child(post.id!!).setValue(true)

            } else {

                FirebaseDatabase.getInstance().reference.child("Saved").child(firebaseUser.uid).child(post.id!!).removeValue()

            }

        }

        checkSave(post, holder.binding)

    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class MySavedViewHolder(val binding: ItemSavedBinding): RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun setPosts(posts: List<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    private fun checkSave(post: Post, binding: ItemSavedBinding) {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Saved").child(firebaseUser.uid)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(post.id!!).exists()) {

                    binding.mSave.setImageResource(R.drawable.ic_saved)
                    binding.mSave.tag = "Saved"

                } else {

                    binding.mSave.setImageResource(R.drawable.ic_save)
                    binding.mSave.tag = "Save"

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    interface OnImageClick {
        fun onClick(post: Post)
    }

    fun setOnImageClick(onImageClick: OnImageClick) {
        this.onImageClick = onImageClick
    }

}