package com.rasenyer.socialnetwork.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.adapters.ImageAdapter
import com.rasenyer.socialnetwork.adapters.UserAdapter
import com.rasenyer.socialnetwork.databinding.FragmentSearchBinding
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.models.User
import com.rasenyer.socialnetwork.ui.activities.CommentActivity
import com.rasenyer.socialnetwork.ui.activities.UserActivity
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_UID
import com.rasenyer.socialnetwork.util.Constants.Companion.USER_UID

class SearchFragment : Fragment() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var userAdapter: UserAdapter
    private lateinit var posts: MutableList<Post>
    private lateinit var users: MutableList<User>

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        imageAdapter = ImageAdapter()
        userAdapter = UserAdapter()
        posts = ArrayList()
        users = ArrayList()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerViews()
        setPosts()
        setUsers()
        setOnSearchViewClick()
        setOnDashboardClick()
        setOnImageClick()
        setOnItemClick()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setRecyclerViews() {

        binding.mRecyclerViewImages.apply {
            adapter = imageAdapter
        }

        binding.mRecyclerViewUsers.apply {
            adapter = userAdapter
        }

    }

    private fun setPosts() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Posts")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                posts.clear()

                for (snapshot in dataSnapshot.children) {

                    val post: Post? = snapshot.getValue(Post::class.java)

                    if (!post!!.uid.equals(firebaseUser.uid)) {

                        posts.add(post)

                    }

                }

                posts.reverse()
                imageAdapter.setPosts(posts)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setUsers() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                users.clear()

                for (snapshot in dataSnapshot.children) {

                    val user: User? = snapshot.getValue(User::class.java)
                    users.add(user!!)

                }

                userAdapter.setUsers(users)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setOnSearchViewClick() {

        binding.mSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener, androidx.appcompat.widget.SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(username: String?): Boolean {

                if (username != null) {

                    showDashboard()
                    searchUsers(username)

                }

                return true

            }

            override fun onQueryTextChange(username: String?): Boolean {
                return true
            }

        })

    }

    private fun setOnDashboardClick() {

        binding.mDashboard.setOnClickListener {

            hideDashboard()

        }

    }

    private fun setOnImageClick() {

        imageAdapter.setOnImageClick(object : ImageAdapter.OnImageClick {

            override fun onClick(post: Post) {

                val intent = Intent(activity, CommentActivity::class.java)
                intent.putExtra(POST_UID, post.uid)
                intent.putExtra(POST_ID, post.id)
                startActivity(intent)

            }

        })

    }

    private fun setOnItemClick() {

        userAdapter.setOnItemClick(object : UserAdapter.OnItemClick {

            override fun onClick(user: User) {

                val intent = Intent(activity, UserActivity::class.java)
                intent.putExtra(USER_UID, user.uid)
                startActivity(intent)

            }

        })

    }

    private fun searchUsers(username: String) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                users.clear()

                for (snapshot in dataSnapshot.children) {

                    val user: User? = snapshot.getValue(User::class.java)

                    if (user!!.username!!.contains(username)) {

                        users.add(user)

                    }

                }

                userAdapter.setUsers(users)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun showDashboard() {

        binding.mDashboard.visibility = View.VISIBLE
        binding.mRecyclerViewUsers.visibility = View.VISIBLE
        binding.mRecyclerViewImages.visibility = View.GONE

    }

    private fun hideDashboard() {

        binding.mDashboard.visibility = View.GONE
        binding.mRecyclerViewUsers.visibility = View.GONE
        binding.mRecyclerViewImages.visibility = View.VISIBLE

    }

}