package com.rasenyer.socialnetwork.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.adapters.SavedAdapter
import com.rasenyer.socialnetwork.databinding.ActivitySavedBinding
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_UID

class SavedActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var savedAdapter: SavedAdapter
    private lateinit var postsId: MutableList<String>
    private lateinit var posts: MutableList<Post>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivitySavedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        savedAdapter = SavedAdapter()
        postsId = ArrayList()
        posts = ArrayList()

        setRecyclerView()
        setPostsId()
        setOnSwipeRefresh()
        setOnImageClick()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun setRecyclerView() {

        binding.mRecyclerView.apply {
            adapter = savedAdapter
        }

    }

    private fun setPostsId() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Saved").child(firebaseUser.uid)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                postsId.clear()

                if (dataSnapshot.exists()) {

                    for (snapshot in dataSnapshot.children) {

                        postsId.add(snapshot.key!!)

                    }

                    setPosts()

                } else {

                    showTextViewNoSavedPosts()

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setPosts() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                posts.clear()

                for (snapshot in dataSnapshot.children) {

                    val post: Post? = snapshot.getValue(Post::class.java)

                    for (postId in postsId) {

                        if (post!!.id.equals(postId)) {

                            posts.add(post)

                        }

                    }

                }

                posts.reverse()
                savedAdapter.setPosts(posts)
                hideTextViewNoSavedPosts()

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setOnSwipeRefresh() {

        binding.mSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.purple_700)
        binding.mSwipeRefresh.setColorSchemeResources(R.color.white)

        binding.mSwipeRefresh.setOnRefreshListener {

            postsId.clear()
            posts.clear()
            setRecyclerView()
            setPostsId()

        }

    }

    private fun setOnImageClick() {

        savedAdapter.setOnImageClick(object : SavedAdapter.OnImageClick {

            override fun onClick(post: Post) {

                val intent = Intent(this@SavedActivity, CommentActivity::class.java)
                intent.putExtra(POST_UID, post.uid)
                intent.putExtra(POST_ID, post.id)
                startActivity(intent)

            }

        })

    }

    private fun showTextViewNoSavedPosts() {

        binding.mTextViewNoSavedPosts.visibility = View.VISIBLE
        binding.mSwipeRefresh.isRefreshing = false

    }

    private fun hideTextViewNoSavedPosts() {

        binding.mTextViewNoSavedPosts.visibility = View.GONE
        binding.mSwipeRefresh.isRefreshing = false

    }

}