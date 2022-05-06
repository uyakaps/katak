package com.rasenyer.socialnetwork.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.adapters.ImageAdapter
import com.rasenyer.socialnetwork.databinding.ActivityImageBinding
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_UID
import com.rasenyer.socialnetwork.util.Constants.Companion.USER_UID

class ImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageBinding
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var posts: MutableList<Post>
    private lateinit var userUid: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageAdapter = ImageAdapter()
        posts = ArrayList()

        val intent = intent
        userUid = intent.getStringExtra(USER_UID)!!

        setNumberPosts()
        setRecyclerView()
        setPosts()
        setOnImageClick()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun setNumberPosts() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0

                for (snapshot in dataSnapshot.children) {

                    val post: Post? = snapshot.getValue(Post::class.java)

                    if (post!!.uid.equals(userUid)) {

                        i++

                    }

                }

                supportActionBar!!.title = i.toString() + " " + resources.getString(R.string.posts)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setRecyclerView() {

        binding.mRecyclerView.apply {
            adapter = imageAdapter
        }

    }

    private fun setPosts() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Posts")
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
                imageAdapter.setPosts(posts)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setOnImageClick() {

        imageAdapter.setOnImageClick(object : ImageAdapter.OnImageClick {

            override fun onClick(post: Post) {

                val intent = Intent(this@ImageActivity, CommentActivity::class.java)
                intent.putExtra(POST_UID, post.uid)
                intent.putExtra(POST_ID, post.id)
                startActivity(intent)

            }

        })

    }

}