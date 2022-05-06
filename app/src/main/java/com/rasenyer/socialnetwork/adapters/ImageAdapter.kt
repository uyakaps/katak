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
import com.rasenyer.socialnetwork.databinding.ItemImageBinding
import com.rasenyer.socialnetwork.models.Post
import java.util.HashMap

class ImageAdapter: RecyclerView.Adapter<ImageAdapter.MyImageViewHolder>() {

    private var posts: List<Post> = ArrayList()
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var onImageClick: OnImageClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyImageViewHolder {
        return MyImageViewHolder(ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyImageViewHolder, position: Int) {

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

        holder.binding.mLike.setOnClickListener {

            if (holder.binding.mLike.tag.equals("Like")) {

                FirebaseDatabase.getInstance().reference.child("Likes").child(post.id!!).child(firebaseUser.uid).setValue(true)
                generateLikeNotification(post)

            } else {

                FirebaseDatabase.getInstance().reference.child("Likes").child(post.id!!).child(firebaseUser.uid).removeValue()

            }

        }

        checkLike(post, holder.binding)

    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class MyImageViewHolder(val binding: ItemImageBinding): RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun setPosts(posts: List<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }

    private fun checkLike(post: Post, binding: ItemImageBinding) {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Likes").child(post.id!!)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(firebaseUser.uid).exists()) {

                    binding.mLike.setImageResource(R.drawable.ic_liked)
                    binding.mLike.tag = "Liked"

                } else {

                    binding.mLike.setImageResource(R.drawable.ic_like)
                    binding.mLike.tag = "Like"

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    private fun generateLikeNotification(post: Post) {

        if (post.uid != firebaseUser.uid) {

            val databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(post.uid!!)

            val hashMap = HashMap<String, Any>()
            hashMap["notificationUid"] = firebaseUser.uid
            hashMap["description"] = "Liked your post"
            hashMap["isPost"] = true
            hashMap["postUid"] = post.uid
            hashMap["postId"] = post.id!!

            databaseReference.push().setValue(hashMap)

        }

    }


    interface OnImageClick {
        fun onClick(post: Post)
    }

    fun setOnImageClick(onImageClick: OnImageClick) {
        this.onImageClick = onImageClick
    }


}