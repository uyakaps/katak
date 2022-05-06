package com.rasenyer.socialnetwork.ui.activities

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import coil.load
import coil.transform.CircleCropTransformation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.adapters.CommentAdapter
import com.rasenyer.socialnetwork.adapters.PostAdapter
import com.rasenyer.socialnetwork.databinding.ActivityCommentBinding
import com.rasenyer.socialnetwork.models.Comment
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.models.User
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_UID
import com.rasenyer.socialnetwork.util.Constants.Companion.TITLE
import com.rasenyer.socialnetwork.util.Constants.Companion.USER_UID

class CommentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCommentBinding
    private lateinit var firebaseUser: FirebaseUser
    private lateinit var postAdapter: PostAdapter
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var posts: MutableList<Post>
    private lateinit var comments: MutableList<Comment>
    private lateinit var commentsUid: MutableList<String>
    private lateinit var postUid: String
    private lateinit var postId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        postAdapter = PostAdapter()
        commentAdapter = CommentAdapter()
        posts = ArrayList()
        comments = ArrayList()
        commentsUid = ArrayList()

        val intent = intent
        postUid = intent.getStringExtra(POST_UID)!!
        postId = intent.getStringExtra(POST_ID)!!

        setRecyclerViews()
        setUserInfo()
        setPost()
        setComments()
        setCommentsUid()
        setOnSendClick()
        setOnProfilePictureClickPostAdapter()
        setOnEditClick()
        setOnNumberLikesClick()
        setOnCommentClick()
        setOnProfilePictureClickCommentAdapter()
        setOnItemLongClick()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun setRecyclerViews() {

        binding.mRecyclerViewPost.apply {
            adapter = postAdapter
        }

        binding.mRecyclerViewComments.apply {
            adapter = commentAdapter
        }

    }

    private fun setUserInfo() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                val user: User? = snapshot.getValue(User::class.java)

                binding.mProfilePicture.load(user!!.profilePicture) {
                    transformations(CircleCropTransformation())
                    placeholder(R.drawable.ic_profile)
                    error(R.drawable.ic_profile)
                    crossfade(true)
                    crossfade(400)
                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setPost() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Posts").child(postId)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {

                    posts.clear()
                    val post: Post? = snapshot.getValue(Post::class.java)
                    posts.add(post!!)
                    postAdapter.setPosts(posts)
                    enableViews()

                } else {

                    posts.clear()
                    disableViews()

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setComments() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                comments.clear()

                for (snapshot in dataSnapshot.children) {

                    val comment: Comment? = snapshot.getValue(Comment::class.java)
                    comments.add(comment!!)

                }

                commentAdapter.setComments(comments)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setCommentsUid() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                for (snapshot in dataSnapshot.children) {

                    val comment: Comment? = snapshot.getValue(Comment::class.java)
                    val commentUid: String = comment!!.uid!!

                    if (commentUid != postUid && commentUid != firebaseUser.uid) {

                        if (!commentsUid.contains(commentUid)) {

                            commentsUid.add(commentUid)

                        }

                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setOnSendClick() {

        binding.mSend.setOnClickListener {

            when {

                TextUtils.isEmpty(binding.mEditTextComment.text.toString()) -> {
                    binding.mEditTextComment.error = resources.getString(R.string.write_a_comment_first)
                }

                else -> {
                    sendComment()
                }

            }

        }

    }

    private fun setOnProfilePictureClickPostAdapter() {

        postAdapter.setOnProfilePictureClick(object : PostAdapter.OnProfilePictureClick {

            override fun onClick(post: Post) {

                val intent = Intent(this@CommentActivity, UserActivity::class.java)
                intent.putExtra(USER_UID, post.uid)
                startActivity(intent)

            }

        })

    }

    private fun setOnEditClick() {

        postAdapter.setOnEditClick(object : PostAdapter.OnEditClick {

            override fun onClick(post: Post) {

                val intent = Intent(this@CommentActivity, PostEditActivity::class.java)
                intent.putExtra(POST_ID, post.id)
                startActivity(intent)

            }

        })

    }

    private fun setOnNumberLikesClick() {

        postAdapter.setOnNumberLikesClick(object : PostAdapter.OnNumberLikesClick {

            override fun onClick(post: Post) {

                val intent = Intent(this@CommentActivity, ListActivity::class.java)
                intent.putExtra(USER_UID, "")
                intent.putExtra(POST_ID, post.id)
                intent.putExtra(TITLE, "Likes")
                startActivity(intent)

            }

        })

    }

    private fun setOnCommentClick() {

        postAdapter.setOnCommentClick(object : PostAdapter.OnCommentClick {

            override fun onClick(post: Post) {}

        })

    }

    private fun setOnProfilePictureClickCommentAdapter() {

        commentAdapter.setOnProfilePictureClick(object : CommentAdapter.OnProfilePictureClick {

            override fun onClick(comment: Comment) {

                val intent = Intent(this@CommentActivity, UserActivity::class.java)
                intent.putExtra(USER_UID, comment.uid)
                startActivity(intent)

            }

        })

    }

    private fun setOnItemLongClick() {

        commentAdapter.setOnItemLongClick(object : CommentAdapter.OnItemLongClick {

            override fun onLongClick(comment: Comment) {

                if (comment.uid.equals(firebaseUser.uid)) {

                    deleteComment(comment)

                }

            }

        })

    }

    private fun deleteComment(comment: Comment) {

        val builder = AlertDialog.Builder(this)
        builder.setIcon(R.drawable.ic_delete)
        builder.setTitle(R.string.delete)
        builder.setMessage(R.string.are_you_sure_you_want_to_delete_your_comment)

        builder.setPositiveButton(R.string.yes) {_: DialogInterface, _: Int ->
            FirebaseDatabase.getInstance().getReference("Comments").child(postId).child(comment.id!!).removeValue()
            Toast.makeText(this, R.string.comment_deleted, Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton(R.string.no) {_, _->}
        builder.create().show()

    }

    private fun sendComment() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Comments").child(postId)
        val id = databaseReference.push().key

        val hashMap = HashMap<String,Any?>()
        hashMap["uid"] = firebaseUser.uid
        hashMap["id"] = id
        hashMap["description"] = binding.mEditTextComment.text.toString()

        databaseReference.child(id!!).setValue(hashMap)
        generateCommentNotification()
        binding.mEditTextComment.setText("")
        Toast.makeText(this, R.string.comment_posted, Toast.LENGTH_SHORT).show()

    }

    private fun generateCommentNotification() {

        if (postUid != firebaseUser.uid) {

            val databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(postUid)

            val hashMap = HashMap<String,Any?>()
            hashMap["notificationUid"] = firebaseUser.uid
            hashMap["description"] = "Commented on your post: " + binding.mEditTextComment.text.toString()
            hashMap["isPost"] = true
            hashMap["postUid"] = postUid
            hashMap["postId"] = postId

            databaseReference.push().setValue(hashMap)

        }

        for (commentUid in commentsUid) {

            val databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(commentUid)

            val hashMap = HashMap<String,Any?>()
            hashMap["notificationUid"] = firebaseUser.uid
            hashMap["description"] = "Also commented: " + binding.mEditTextComment.text.toString()
            hashMap["isPost"] = true
            hashMap["postUid"] = postUid
            hashMap["postId"] = postId

            databaseReference.push().setValue(hashMap)

        }

    }

    private fun enableViews() {

        binding.mEditTextComment.isEnabled = true
        binding.mSend.isEnabled = true
        binding.mTextViewPostDeleted.visibility = View.GONE

    }

    private fun disableViews() {

        binding.mEditTextComment.isEnabled = false
        binding.mSend.isEnabled = false
        binding.mTextViewPostDeleted.visibility = View.VISIBLE

    }

}