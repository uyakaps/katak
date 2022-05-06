package com.rasenyer.socialnetwork.ui.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.databinding.ActivityPostEditBinding
import com.rasenyer.socialnetwork.models.Post
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID

class PostEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostEditBinding
    private lateinit var postId: String
    private var imagePostUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityPostEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val intent = intent
        postId = intent.getStringExtra(POST_ID)!!

        setPostInfo()
        onEditClick()
        onUpdateClick()
        onDeleteClick()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun setPostInfo() {

        val databaseReference = FirebaseDatabase.getInstance().reference.child("Posts").child(postId)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                binding.mEditTextDescription.setText(snapshot.getValue(Post::class.java)!!.description)

                binding.mImagePost.load(snapshot.getValue(Post::class.java)!!.image) {
                    placeholder(R.color.purple_200)
                    error(R.color.purple_200)
                    crossfade(true)
                    crossfade(400)
                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun onEditClick() {

        binding.mEdit.setOnClickListener {
            ImagePicker.with(this as Activity).crop().createIntentFromDialog { launcherImage.launch(it) }
        }

    }

    private val launcherImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        if (it.resultCode == Activity.RESULT_OK) {

            imagePostUri = it.data?.data!!

            binding.mImagePost.load(imagePostUri) {
                placeholder(R.color.purple_200)
                error(R.color.purple_200)
                crossfade(true)
                crossfade(400)
            }

        }

    }

    private fun onUpdateClick() {

        binding.mUpdate.setOnClickListener {

            when {

                TextUtils.isEmpty(binding.mEditTextDescription.text.toString()) -> {
                    binding.mEditTextDescription.error = resources.getString(R.string.enter_a_description)
                }

                else -> {
                    updatePost()
                }

            }

        }

    }

    private fun updatePost() {

        disableViews()

        if (imagePostUri == null) {

            val hashMap = HashMap<String, Any>()
            hashMap["description"] = binding.mEditTextDescription.text.toString()

            FirebaseDatabase.getInstance().getReference("Posts").child(postId).updateChildren(hashMap)
            enableViews()
            Toast.makeText(this, R.string.updated_post, Toast.LENGTH_SHORT).show()
            finish()

        } else {

            val storageReference = FirebaseStorage.getInstance().reference.child("PostImages").child(System.currentTimeMillis().toString() + ".jpg")

            val storageTask: StorageTask<*>
            storageTask = storageReference.putFile(imagePostUri!!)

            storageTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }

                return@Continuation storageReference.downloadUrl

            }).addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    val downloadUrl = task.result
                    val imageUrl = downloadUrl.toString()

                    val hashMap = HashMap<String, Any>()
                    hashMap["description"] = binding.mEditTextDescription.text.toString()
                    hashMap["image"] = imageUrl

                    FirebaseDatabase.getInstance().getReference("Posts").child(postId).updateChildren(hashMap)
                    enableViews()
                    Toast.makeText(this, R.string.updated_post, Toast.LENGTH_SHORT).show()
                    finish()

                }

            }

        }

    }

    private fun onDeleteClick() {

        binding.mDelete.setOnClickListener {

            val builder = AlertDialog.Builder(this)
            builder.setIcon(R.drawable.ic_delete)
            builder.setTitle(R.string.delete)
            builder.setMessage(R.string.are_you_sure_you_want_to_delete_your_post)
            builder.setPositiveButton(R.string.yes) { _: DialogInterface, _: Int -> deletePost()}
            builder.setNegativeButton(R.string.no) { _, _ ->}
            builder.create().show()

        }

    }

    private fun deletePost() {

        FirebaseDatabase.getInstance().getReference("Posts").child(postId).removeValue()
        FirebaseDatabase.getInstance().getReference("Comments").child(postId).removeValue()
        Toast.makeText(this, R.string.post_deleted, Toast.LENGTH_SHORT).show()
        finish()

    }

    private fun enableViews() {

        binding.mEditTextDescription.isEnabled = true
        binding.mEdit.isEnabled = true
        binding.mUpdate.isEnabled = true
        binding.mDelete.isEnabled = true
        binding.mProgressBar.visibility = View.GONE

    }

    private fun disableViews() {

        binding.mEditTextDescription.isEnabled = false
        binding.mEdit.isEnabled = false
        binding.mUpdate.isEnabled = false
        binding.mDelete.isEnabled = false
        binding.mProgressBar.visibility = View.VISIBLE

    }

}