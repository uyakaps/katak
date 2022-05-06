package com.rasenyer.socialnetwork.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.databinding.ItemNotificationBinding
import com.rasenyer.socialnetwork.models.Notification
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.models.User

class NotificationAdapter: RecyclerView.Adapter<NotificationAdapter.MyNotificationViewHolder>() {

    private var notifications: List<Notification> = ArrayList()
    private var postUid: String? = null
    private lateinit var onItemClick: OnItemClick

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyNotificationViewHolder {
        return MyNotificationViewHolder(ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: MyNotificationViewHolder, position: Int) {

        val notification = notifications[position]

        when {

            notification.description.equals("Started following you") -> {

                holder.binding.mLiked.visibility = View.GONE
                holder.binding.mImagePost.visibility = View.GONE

            }

            notification.description.equals("Liked your post") -> {

                holder.binding.mLiked.visibility = View.VISIBLE
                holder.binding.mImagePost.visibility = View.VISIBLE

            }

            else -> {

                holder.binding.mLiked.visibility = View.GONE
                holder.binding.mImagePost.visibility = View.VISIBLE

            }

        }

        holder.binding.mDescription.text = notification.description

        holder.itemView.setOnClickListener {
            onItemClick.onClick(notification)
        }

        setUserInfo(notification, holder.binding)
        getPostUidAndSetImagePost(notification, holder.binding)

    }

    override fun getItemCount(): Int {
        return notifications.size
    }

    class MyNotificationViewHolder(val binding: ItemNotificationBinding): RecyclerView.ViewHolder(binding.root)

    @SuppressLint("NotifyDataSetChanged")
    fun setNotifications(notifications: List<Notification>) {
        this.notifications = notifications
        notifyDataSetChanged()
    }


    private fun setUserInfo(notification: Notification, binding: ItemNotificationBinding) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(notification.notificationUid!!)
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

    private fun getPostUidAndSetImagePost(notification: Notification, binding: ItemNotificationBinding) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(notification.postId!!)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    val post: Post? = snapshot.getValue(Post::class.java)

                    postUid = post!!.uid

                    binding.mImagePost.load(post.image) {
                        placeholder(R.color.purple_200)
                        error(R.color.purple_200)
                        crossfade(true)
                        crossfade(400)
                    }

                } else {

                    binding.mImagePost.load(R.drawable.ic_error)

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    interface OnItemClick {
        fun onClick(notification: Notification)
    }

    fun setOnItemClick(onItemClick: OnItemClick) {
        this.onItemClick = onItemClick
    }

}