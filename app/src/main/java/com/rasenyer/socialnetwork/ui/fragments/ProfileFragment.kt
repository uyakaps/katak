package com.rasenyer.socialnetwork.ui.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.rasenyer.socialnetwork.databinding.FragmentProfileBinding
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.models.User
import com.rasenyer.socialnetwork.ui.activities.*
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_UID
import com.rasenyer.socialnetwork.util.Constants.Companion.TITLE
import com.rasenyer.socialnetwork.util.Constants.Companion.USER_UID

class ProfileFragment : Fragment() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var postAdapter: PostAdapter
    private lateinit var posts: MutableList<Post>

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        postAdapter = PostAdapter()
        posts = ArrayList()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUserInfo()
        setNumberPosts()
        setNumberFollowers()
        setNumberFollowing()
        setRecyclerView()
        setPosts()
        setOnNumberPostsClick()
        setOnNumberFollowersClick()
        setOnNumberFollowingClick()
        setOnOptionsClick()
        setOnProfilePictureClick()
        setOnEditClick()
        setOnNumberLikesClick()
        setOnCommentClick()
        setOnSwipeRefresh()

    }

    override fun onResume() {
        super.onResume()

        setUserInfo()

    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null

    }

    private fun setUserInfo() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
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

    private fun setNumberPosts() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var i = 0

                for (snapshot in dataSnapshot.children) {

                    val post: Post? = snapshot.getValue(Post::class.java)

                    if (post!!.uid.equals(firebaseUser.uid)) {

                        i++

                    }

                }

                binding.mNumberPosts.text = i.toString() + " " + resources.getString(R.string.posts)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setNumberFollowers() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Followers")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            @SuppressLint("SetTextI18n")
            override fun onDataChange(snapshot: DataSnapshot) {

                binding.mNumberFollowers.text = "" + snapshot.childrenCount + " " + resources.getString(R.string.followers)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setNumberFollowing() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Follow").child(firebaseUser.uid).child("Following")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

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

                    if (post!!.uid.equals(firebaseUser.uid)) {

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

            val intent = Intent(activity, ImageActivity::class.java)
            intent.putExtra(USER_UID, firebaseUser.uid)
            startActivity(intent)

        }

    }

    private fun setOnNumberFollowersClick() {

        binding.mNumberFollowers.setOnClickListener {

            val intent = Intent(activity, ListActivity::class.java)
            intent.putExtra(USER_UID, firebaseUser.uid)
            intent.putExtra(POST_ID, "")
            intent.putExtra(TITLE, "Followers")
            startActivity(intent)

        }

    }

    private fun setOnNumberFollowingClick() {

        binding.mNumberFollowing.setOnClickListener {

            val intent = Intent(activity, ListActivity::class.java)
            intent.putExtra(USER_UID, firebaseUser.uid)
            intent.putExtra(POST_ID, "")
            intent.putExtra(TITLE, "Following")
            startActivity(intent)

        }

    }

    private fun setOnOptionsClick() {

        binding.mOptions.setOnClickListener {

            startActivity(Intent(activity, OptionsActivity::class.java))

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

    private fun setOnSwipeRefresh() {

        binding.mSwipeRefresh.setProgressBackgroundColorSchemeResource(R.color.purple_700)
        binding.mSwipeRefresh.setColorSchemeResources(R.color.white)

        binding.mSwipeRefresh.setOnRefreshListener {

            setNumberPosts()
            setNumberFollowers()
            setNumberFollowing()
            binding.mSwipeRefresh.isRefreshing = false

        }

    }

}