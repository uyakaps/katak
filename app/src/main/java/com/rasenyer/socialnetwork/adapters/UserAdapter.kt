package com.rasenyer.socialnetwork.adapters

import android.annotation.SuppressLint
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
import com.rasenyer.socialnetwork.databinding.ItemUserBinding
import com.rasenyer.socialnetwork.models.User

class UserAdapter: RecyclerView.Adapter<UserAdapter.MyUserViewHolder>() {

    private var users: List<User> = ArrayList()
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var onItemClick: OnItemClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyUserViewHolder {
        return MyUserViewHolder(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyUserViewHolder, position: Int) {

        val user = users[position]
        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        if (!user.uid.equals(firebaseUser.uid)) {
            holder.binding.mFollow.visibility = View.VISIBLE
            holder.itemView.isEnabled = true
        } else {
            holder.binding.mFollow.visibility = View.GONE
            holder.itemView.isEnabled = false
        }

        holder.binding.apply {

            mProfilePicture.load(user.profilePicture) {
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                crossfade(true)
                crossfade(400)
            }
            mUsername.text = user.username

        }

        holder.itemView.setOnClickListener {
            onItemClick.onClick(user)
        }

        holder.binding.mFollow.setOnClickListener {

            if (holder.binding.mFollow.text.toString() == "Follow") {

                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following").child(user.uid!!).setValue(true)
                FirebaseDatabase.getInstance().reference.child("Follow").child(user.uid).child("Followers").child(firebaseUser.uid).setValue(true)
                generateFollowNotification(user)

            } else {

                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following").child(user.uid!!).removeValue()
                FirebaseDatabase.getInstance().reference.child("Follow").child(user.uid).child("Followers").child(firebaseUser.uid).removeValue()

            }

        }

        checkFollow(user, holder.binding)

    }

    override fun getItemCount(): Int {
        return users.size
    }

    class MyUserViewHolder(val binding: ItemUserBinding): RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun setUsers(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }

    private fun checkFollow(user: User, binding: ItemUserBinding) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.uid).child("Following")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(user.uid!!).exists()) {
                    binding.mFollow.setText(R.string.following)
                } else {
                    binding.mFollow.setText(R.string.follow)
                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun generateFollowNotification(user: User) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(user.uid!!)

        val hashMap = HashMap<String, Any>()
        hashMap["notificationUid"] = firebaseUser.uid
        hashMap["description"] = "Started following you"
        hashMap["isPost"] = false
        hashMap["postUid"] = ""
        hashMap["postId"] = ""

        databaseReference.push().setValue(hashMap)

    }


    interface OnItemClick {
        fun onClick(user: User)
    }

    fun setOnItemClick(onItemClick: OnItemClick) {
        this.onItemClick = onItemClick
    }


}