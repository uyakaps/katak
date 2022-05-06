package com.rasenyer.socialnetwork.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.adapters.PostAdapter
import com.rasenyer.socialnetwork.databinding.FragmentHomeBinding
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.ui.activities.CommentActivity
import com.rasenyer.socialnetwork.ui.activities.ListActivity
import com.rasenyer.socialnetwork.ui.activities.PostEditActivity
import com.rasenyer.socialnetwork.ui.activities.UserActivity
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_UID
import com.rasenyer.socialnetwork.util.Constants.Companion.TITLE
import com.rasenyer.socialnetwork.util.Constants.Companion.USER_UID

class HomeFragment : Fragment() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var postAdapter: PostAdapter
    private lateinit var followingUsers: MutableList<String>
    private lateinit var followingPosts: MutableList<Post>

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        postAdapter = PostAdapter()
        followingUsers = ArrayList()
        followingPosts = ArrayList()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
        setFollowingUsers()
        setOnSwipeRefresh()
        setOnProfilePictureClick()
        setOnEditClick()
        setOnNumberLikesClick()
        setOnCommentClick()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setRecyclerView() {

        binding.mRecyclerView.apply {
            adapter = postAdapter
        }

    }

    private fun setFollowingUsers() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Follow").child(firebaseUser.uid).child("Following")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                followingUsers.clear()

                if (dataSnapshot.exists()) {

                    for (snapshot in dataSnapshot.children) {

                        followingUsers.add(snapshot.key!!)

                    }

                    setPosts()

                } else {

                    showTextViewNoPosts()

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setOnSwipeRefresh() {

        binding.mSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.purple_700)
        binding.mSwipeRefresh.setColorSchemeResources(R.color.white)

        binding.mSwipeRefresh.setOnRefreshListener {

            followingUsers.clear()
            followingPosts.clear()
            setRecyclerView()
            setFollowingUsers()

        }

    }

    private fun setOnProfilePictureClick() {

        postAdapter.setOnProfilePictureClick(object : PostAdapter.OnProfilePictureClick {

            override fun onClick(post: Post) {

                val intent = Intent(activity, UserActivity::class.java)
                intent.putExtra(USER_UID, post.uid)
                startActivity(intent)

            }

        })

    }

    private fun setOnEditClick() {

        postAdapter.setOnEditClick(object : PostAdapter.OnEditClick {

            override fun onClick(post: Post) {

                val intent = Intent(activity, PostEditActivity::class.java)
                intent.putExtra(POST_ID, post.id)
                startActivity(intent)

            }

        })

    }

    private fun setOnNumberLikesClick() {

        postAdapter.setOnNumberLikesClick(object : PostAdapter.OnNumberLikesClick {

            override fun onClick(post: Post) {

                val intent = Intent(activity, ListActivity::class.java)
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

                val intent = Intent(activity, CommentActivity::class.java)
                intent.putExtra(POST_UID, post.uid)
                intent.putExtra(POST_ID, post.id)
                startActivity(intent)

            }

        })

    }

    private fun setPosts() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Posts")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                followingPosts.clear()

                for (snapshot in dataSnapshot.children) {

                    val post: Post? = snapshot.getValue(Post::class.java)

                    for (uid in followingUsers) {

                        if (post!!.uid.equals(uid)) {

                            followingPosts.add(post)

                        }

                    }

                }

                followingPosts.reverse()
                postAdapter.setPosts(followingPosts)
                hideTextViewNoPosts()

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun showTextViewNoPosts() {

        binding.mTextViewNoPosts.visibility = View.VISIBLE
        binding.mSwipeRefresh.isRefreshing = false
        binding.mProgressBar.visibility = View.GONE

    }

    private fun hideTextViewNoPosts() {

        binding.mTextViewNoPosts.visibility = View.GONE
        binding.mSwipeRefresh.isRefreshing = false
        binding.mProgressBar.visibility = View.GONE

    }

}