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
import com.rasenyer.socialnetwork.adapters.UserAdapter
import com.rasenyer.socialnetwork.databinding.ActivityListBinding
import com.rasenyer.socialnetwork.models.User
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.TITLE
import com.rasenyer.socialnetwork.util.Constants.Companion.USER_UID

class ListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityListBinding
    private lateinit var userAdapter: UserAdapter
    private lateinit var likesUid: MutableList<String>
    private lateinit var followersUid: MutableList<String>
    private lateinit var followingUid: MutableList<String>
    private lateinit var users: MutableList<User>
    private lateinit var userUid: String
    private lateinit var postId: String
    private lateinit var title: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userAdapter = UserAdapter()
        likesUid = ArrayList()
        followersUid = ArrayList()
        followingUid = ArrayList()
        users = ArrayList()

        val intent = intent
        userUid = intent.getStringExtra(USER_UID)!!
        postId = intent.getStringExtra(POST_ID)!!
        title = intent.getStringExtra(TITLE)!!

        when (title) {
            "Likes" -> { setLikesUid() }
            "Followers" -> { setFollowersUid() }
            "Following" -> { setFollowingUid() }
        }

        setRecyclerView()
        setOnItemClick()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun setRecyclerView() {

        binding.mRecyclerView.apply {
            adapter = userAdapter
        }

    }


    private fun setLikesUid() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Likes").child(postId)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                supportActionBar!!.title = dataSnapshot.childrenCount.toString() + " " + resources.getString(R.string.likes)

                likesUid.clear()

                for (snapshot in dataSnapshot.children) {

                    likesUid.add(snapshot.key!!)

                }

                setUsers1()

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setUsers1() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                users.clear()

                for (snapshot in dataSnapshot.children) {

                    val user: User? = snapshot.getValue(User::class.java)

                    for (likeUid in likesUid) {

                        if (user!!.uid.equals(likeUid)) {

                            users.add(user)

                        }

                    }

                }

                userAdapter.setUsers(users)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    private fun setFollowersUid() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Follow").child(userUid).child("Followers")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                supportActionBar!!.title = dataSnapshot.childrenCount.toString() + " " + resources.getString(R.string.followers)

                followersUid.clear()

                for (snapshot in dataSnapshot.children) {

                    followersUid.add(snapshot.key!!)

                }

                setUsers2()

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setUsers2() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                users.clear()

                for (snapshot in dataSnapshot.children) {

                    val user: User? = snapshot.getValue(User::class.java)

                    for (followerUid in followersUid) {

                        if (user!!.uid.equals(followerUid)) {

                            users.add(user)

                        }

                    }

                }

                userAdapter.setUsers(users)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    private fun setFollowingUid() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Follow").child(userUid).child("Following")
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                supportActionBar!!.title = dataSnapshot.childrenCount.toString() + " " + resources.getString(R.string.following)

                followingUid.clear()

                for (snapshot in dataSnapshot.children) {

                    followingUid.add(snapshot.key!!)

                }

                setUsers3()

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setUsers3() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                users.clear()

                for (snapshot in dataSnapshot.children) {

                    val user: User? = snapshot.getValue(User::class.java)

                    for (uid in followingUid) {

                        if (user!!.uid.equals(uid)) {

                            users.add(user)

                        }

                    }

                }

                userAdapter.setUsers(users)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }


    private fun setOnItemClick() {

        userAdapter.setOnItemClick(object : UserAdapter.OnItemClick {

            override fun onClick(user: User) {

                val intent = Intent(this@ListActivity, UserActivity::class.java)
                intent.putExtra(USER_UID, user.uid)
                startActivity(intent)

            }

        })

    }

}