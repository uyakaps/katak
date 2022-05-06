package com.rasenyer.socialnetwork.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.adapters.PostAdapter
import com.rasenyer.socialnetwork.databinding.ActivityUserBinding
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.models.User
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_UID
import com.rasenyer.socialnetwork.util.Constants.Companion.TITLE
import com.rasenyer.socialnetwork.util.Constants.Companion.USER_UID
import java.util.HashMap

class UserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var postAdapter: PostAdapter
    private lateinit var posts: MutableList<Post>
    private lateinit var userUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        postAdapter = PostAdapter()
        posts = ArrayList()

        val intent = intent
        userUid = intent.getStringExtra(USER_UID)!!

        setUserInfo()
        setNumberPosts()
        setNumberFollowers()
        setNumberFollowing()
        setRecyclerView()
        setPosts()
        setOnNumberPostsClick()
        setOnNumberFollowersClick()
        setOnNumberFollowingClick()
        setOnFollowClick()
        setOnProfilePictureClick()
        setOnEditClick()
        setOnNumberLikesClick()
        setOnCommentClick()
        checkFollow()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun setUserInfo() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(userUid)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val user: User? = snapshot.getValue(User::class.java)

                supportActionBar!!.title = user!!.username

                binding.mProfilePicture.load(user.profilePicture) {
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

    private fun setNumberPosts() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addValueEventListener(object : ValueEventListener {

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0

                for (snapshot in dataSnapshot.children) {

                    val post: Post? = snapshot.getValue(Post::class.java)

                    if (post!!.uid.equals(userUid)) {

                        i++

                    }

                }

                binding.mNumberPosts.text = i.toString() + " " + resources.getString(R.string.posts)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setNumberFollowers() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Follow").child(userUid).child("Followers")
        databaseReference.addValueEventListener(object : ValueEventListener {

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {

                binding.mNumberFollowers.text = "" + snapshot.childrenCount + " " + resources.getString(R.string.followers)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setNumberFollowing() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Follow").child(userUid).child("Following")
        databaseReference.addValueEventListener(object : ValueEventListener {

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {

                binding.mNumberFollowing.text = "" + snapshot.childrenCount + " " + resources.getString(R.string.following)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setRecyclerView() {

        binding.mRecyclerView.apply {
            adapter = postAdapter
        }

    }

    private fun setPosts() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                posts.clear()

                for (snapshot in dataSnapshot.children) {

                    val post: Post? = snapshot.getValue(Post::class.java)

                    if (post!!.uid.equals(userUid)) {

                        posts.add(post)

                    }

                }

                posts.reverse()
                postAdapter.setPosts(posts)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setOnNumberPostsClick() {

        binding.mNumberPosts.setOnClickListener {

            val intent = Intent(this, ImageActivity::class.java)
            intent.putExtra(USER_UID, userUid)
            startActivity(intent)

        }

    }

    private fun setOnNumberFollowersClick() {

        binding.mNumberFollowers.setOnClickListener {

            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(USER_UID, userUid)
            intent.putExtra(POST_ID, "")
            intent.putExtra(TITLE, "Followers")
            startActivity(intent)

        }

    }

    private fun setOnNumberFollowingClick() {

        binding.mNumberFollowing.setOnClickListener {

            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(USER_UID, userUid)
            intent.putExtra(POST_ID, "")
            intent.putExtra(TITLE, "Following")
            startActivity(intent)

        }

    }

    private fun setOnFollowClick() {

        binding.mFollow.setOnClickListener {

            if (binding.mFollow.text.toString() == "Follow") {

                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following").child(userUid).setValue(true)
                FirebaseDatabase.getInstance().reference.child("Follow").child(userUid).child("Followers").child(firebaseUser.uid).setValue(true)
                generateFollowNotification()

            } else {

                FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following").child(userUid).removeValue()
                FirebaseDatabase.getInstance().reference.child("Follow").child(userUid).child("Followers").child(firebaseUser.uid).removeValue()

            }

        }

    }

    private fun setOnProfilePictureClick() {

        postAdapter.setOnProfilePictureClick(object : PostAdapter.OnProfilePictureClick {

            override fun onClick(post: Post) {}

        })

    }

    private fun setOnEditClick() {

        postAdapter.setOnEditClick(object : PostAdapter.OnEditClick {

            override fun onClick(post: Post) {

                val intent = Intent(this@UserActivity, PostEditActivity::class.java)
                intent.putExtra(POST_ID, post.id)
                startActivity(intent)

            }

        })

    }

    private fun setOnNumberLikesClick() {

        postAdapter.setOnNumberLikesClick(object : PostAdapter.OnNumberLikesClick {

            override fun onClick(post: Post) {

                val intent = Intent(this@UserActivity, ListActivity::class.java)
                intent.putExtra(USER_UID, "")
                intent.putExtra(POST_ID, post.id)
                intent.putExtra(TITLE, "Likes")
                startActivity(intent)

            }

        })

    }

    private fun setOnCommentClick() {

        postAdapter.setOnCommentClick(object : PostAdapter.OnCommentClick {

            override fun onClick(post: Post) {

                val intent = Intent(this@UserActivity, CommentActivity::class.java)
                intent.putExtra(POST_UID, post.uid)
                intent.putExtra(POST_ID, post.id)
                startActivity(intent)

            }

        })

    }

    private fun checkFollow() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.child(userUid).exists()) {

                    binding.mFollow.setText(R.string.following)

                } else {

                    binding.mFollow.setText(R.string.follow)

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun generateFollowNotification() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(userUid)

        val hashMap = HashMap<String, Any>()
        hashMap["notificationUid"] = firebaseUser.uid
        hashMap["description"] = "Started following you"
        hashMap["isPost"] = false
        hashMap["postUid"] = ""
        hashMap["postId"] = ""

        databaseReference.push().setValue(hashMap)

    }

}