package com.rasenyer.socialnetwork.adapters

import android.annotation.SuppressLint
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.databinding.ItemPostBinding
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.models.User
import java.util.*
import kotlin.collections.ArrayList

class PostAdapter: RecyclerView.Adapter<PostAdapter.MyPostViewHolder>() {

    private var posts: List<Post> = ArrayList()
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var onProfilePictureClick: OnProfilePictureClick
    private lateinit var onEditClick: OnEditClick
    private lateinit var onNumberLikesClick: OnNumberLikesClick
    private lateinit var onCommentClick: OnCommentClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyPostViewHolder {
        return MyPostViewHolder(ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyPostViewHolder, position: Int) {

        val post = posts[position]
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (!post.uid.equals(firebaseUser.uid)) {
            holder.binding.mFollow.visibility = View.VISIBLE
            holder.binding.mEdit.visibility = View.GONE
            holder.binding.mProfilePicture.isEnabled = true
        } else {
            holder.binding.mFollow.visibility = View.GONE
            holder.binding.mEdit.visibility = View.VISIBLE
            holder.binding.mProfilePicture.isEnabled = false
        }

        holder.binding.apply {

            val calendar = Calendar.getInstance(Locale.getDefault())
            calendar.timeInMillis = post.date!!.toLong()
            val date = DateFormat.format("dd/MM/yyyy", calendar).toString()

            mDate.text = date
            mDescription.text = post.description
            mImagePost.load(post.image) {
                placeholder(R.color.purple_200)
                error(R.color.purple_200)
                crossfade(true)
                crossfade(400)
            }

        }

        holder.binding.mProfilePicture.setOnClickListener {
            onProfilePictureClick.onClick(post)
        }

        holder.binding.mEdit.setOnClickListener {
            onEditClick.onClick(post)
        }

        holder.binding.mNumberLikes.setOnClickListener {
            onNumberLikesClick.onClick(post)
        }

        holder.binding.mComment.setOnClickListener {
            onCommentClick.onClick(post)
        }

        holder.binding.mFollow.setOnClickListener {

            if (holder.binding.mFollow.text.toString() == "Follow") {

                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following").child(post.uid!!).setValue(true)
                FirebaseDatabase.getInstance().reference.child("Follow").child(post.uid).child("Followers").child(firebaseUser.uid).setValue(true)
                generateFollowNotification(post)

            } else {

                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following").child(post.uid!!).removeValue()
                FirebaseDatabase.getInstance().reference.child("Follow").child(post.uid).child("Followers").child(firebaseUser.uid).removeValue()

            }

        }

        holder.binding.mLike.setOnClickListener {

            if (holder.binding.mLike.tag.equals("Like")) {

                FirebaseDatabase.getInstance().reference.child("Likes").child(post.id!!).child(firebaseUser.uid).setValue(true)
                generateLikeNotification(post)

            } else {

                FirebaseDatabase.getInstance().reference.child("Likes").child(post.id!!).child(firebaseUser.uid).removeValue()

            }

        }

        holder.binding.mSave.setOnClickListener {

            if (holder.binding.mSave.tag == "Save") {

                FirebaseDatabase.getInstance().reference.child("Saved").child(firebaseUser.uid).child(post.id!!).setValue(true)

            } else {

                FirebaseDatabase.getInstance().reference.child("Saved").child(firebaseUser.uid).child(post.id!!).removeValue()

            }

        }

        setUserInfo(post, holder.binding)
        setNumberLikes(post, holder.binding)
        setNumberComments(post, holder.binding)
        checkFollow(post, holder.binding)
        checkLike(post, holder.binding)
        checkSave(post, holder.binding)

    }

    override fun getItemCount(): Int {
        return posts.size
    }

    class MyPostViewHolder(val binding: ItemPostBinding): RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun setPosts(posts: List<Post>) {
        this.posts = posts
        notifyDataSetChanged()
    }


    private fun setUserInfo(post: Post, binding: ItemPostBinding) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(post.uid!!)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val user: User? = snapshot.getValue(User::class.java)

                binding.apply {

                    mProfilePicture.load(user!!.profilePicture) {
                        transformations(CircleCropTransformation())
                        placeholder(R.drawable.ic_profile)
                        error(R.drawable.ic_profile)
                        crossfade(true)
                        crossfade(400)
                    }
                    mUsername.text = user.username

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setNumberLikes(post: Post, binding: ItemPostBinding) {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Likes").child(post.id!!)
        databaseReference.addValueEventListener(object : ValueEventListener {

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {

                binding.mNumberLikes.text = snapshot.childrenCount.toString() + " likes"

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setNumberComments(post: Post, binding: ItemPostBinding) {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Comments").child(post.id!!)
        databaseReference.addValueEventListener(object : ValueEventListener {

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {

                binding.mNumberComments.text = snapshot.childrenCount.toString() + " comments"

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun checkFollow(post: Post, binding: ItemPostBinding) {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(post.uid!!).exists()) {

                    binding.mFollow.setText(R.string.following)

                } else {

                    binding.mFollow.setText(R.string.follow)

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun checkLike(post: Post, binding: ItemPostBinding) {

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

    private fun checkSave(post: Post, binding: ItemPostBinding) {

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


    private fun generateFollowNotification(post: Post) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(post.uid!!)

        val hashMap = HashMap<String, Any>()
        hashMap["notificationUid"] = firebaseUser.uid
        hashMap["description"] = "Started following you"
        hashMap["isPost"] = false
        hashMap["postUid"] = ""
        hashMap["postId"] = ""

        databaseReference.push().setValue(hashMap)

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


    interface OnProfilePictureClick {
        fun onClick(post: Post)
    }

    fun setOnProfilePictureClick(onProfilePictureClick: OnProfilePictureClick) {
        this.onProfilePictureClick = onProfilePictureClick
    }


    interface OnEditClick {
        fun onClick(post: Post)
    }

    fun setOnEditClick(onEditClick: OnEditClick) {
        this.onEditClick = onEditClick
    }


    interface OnNumberLikesClick {
        fun onClick(post: Post)
    }

    fun setOnNumberLikesClick(onNumberLikesClick: OnNumberLikesClick) {
        this.onNumberLikesClick = onNumberLikesClick
    }


    interface OnCommentClick {
        fun onClick(post: Post)
    }

    fun setOnCommentClick(onCommentClick: OnCommentClick) {
        this.onCommentClick = onCommentClick
    }


}