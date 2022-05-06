package com.rasenyer.socialnetwork.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
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
import com.rasenyer.socialnetwork.databinding.ItemCommentBinding
import com.rasenyer.socialnetwork.models.Comment
import com.rasenyer.socialnetwork.models.User

class CommentAdapter: RecyclerView.Adapter<CommentAdapter.MyCommentViewHolder>() {

    private var comments: List<Comment> = ArrayList()
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var onProfilePictureClick: OnProfilePictureClick
    private lateinit var onItemLongClick: OnItemLongClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyCommentViewHolder {
        return MyCommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyCommentViewHolder, position: Int) {

        val comment = comments[position]
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        holder.binding.mProfilePicture.isEnabled = !comment.uid.equals(firebaseUser.uid)

        holder.binding.mDescription.text = comment.description

        holder.binding.mProfilePicture.setOnClickListener {
            onProfilePictureClick.onClick(comment)
        }

        holder.itemView.setOnLongClickListener {
            onItemLongClick.onLongClick(comment)
            true
        }

        setUserInfo(comment, holder.binding)

    }

    override fun getItemCount(): Int {
        return comments.size
    }

    class MyCommentViewHolder(val binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun setComments(comments: List<Comment>) {
        this.comments = comments
        notifyDataSetChanged()
    }


    private fun setUserInfo(comment: Comment, binding: ItemCommentBinding) {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Users").child(comment.uid!!)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val user: User? = snapshot.getValue(User::class.java)

                binding.mProfilePicture.load(user!!.profilePicture) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_profile)
                    error(R.drawable.ic_profile)
                    crossfade(true)
                    crossfade(400)
                }
                binding.mUsername.text = user.username

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    interface OnProfilePictureClick {
        fun onClick(comment: Comment)
    }

    fun setOnProfilePictureClick(onProfilePictureClick: OnProfilePictureClick) {
        this.onProfilePictureClick = onProfilePictureClick
    }


    interface OnItemLongClick {
        fun onLongClick(comment: Comment)
    }

    fun setOnItemLongClick(onItemLongClick: OnItemLongClick) {
        this.onItemLongClick = onItemLongClick
    }


}