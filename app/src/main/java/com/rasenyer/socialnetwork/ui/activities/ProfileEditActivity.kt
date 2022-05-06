package com.rasenyer.socialnetwork.ui.activities

import android.app.Activity
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import coil.transform.CircleCropTransformation
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.databinding.ActivityProfileEditBinding
import com.rasenyer.socialnetwork.models.User

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private lateinit var firebaseUser: FirebaseUser
    private var profilePictureUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

        setUserInfo()
        onEditClick()
        onSaveClick()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
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
                binding.mEditTextUsername.setText(user.username)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun onEditClick() {

        binding.mEdit.setOnClickListener {

            ImagePicker.with(this as Activity).crop().createIntentFromDialog { launcherProfilePicture.launch(it) }

        }

    }

    private val launcherProfilePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        if (it.resultCode == Activity.RESULT_OK) {

            profilePictureUri = it.data?.data!!

            binding.mProfilePicture.load(profilePictureUri) {
                transformations(CircleCropTransformation())
                placeholder(R.drawable.ic_profile)
                error(R.drawable.ic_profile)
                crossfade(true)
                crossfade(400)
            }

            updateProfilePicture()

        }

    }

    private fun updateProfilePicture() {

        disableViews()

        val storageReference = FirebaseStorage.getInstance().reference.child("ProfilePictures").child(System.currentTimeMillis().toString() + ".jpg")

        val storageTask: StorageTask<*>
        storageTask = storageReference.putFile(profilePictureUri!!)

        storageTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }

            return@Continuation storageReference.downloadUrl

        }).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                val downloadUrl = task.result
                val urlProfilePicture = downloadUrl.toString()

                val hashMap = HashMap<String, Any>()
                hashMap["profilePicture"] = urlProfilePicture

                FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid).updateChildren(hashMap)
                Toast.makeText(this, R.string.updated_profile_picture, Toast.LENGTH_SHORT).show()
                enableViews()

            }

        }

    }

    private fun onSaveClick() {

        binding.mSave.setOnClickListener {

            when {

                TextUtils.isEmpty(binding.mEditTextUsername.text.toString()) -> {
                    binding.mEditTextUsername.error = resources.getString(R.string.enter_a_username)
                }

                else -> {
                    checkUsername(binding.mEditTextUsername.text.toString())
                }

            }

        }

    }

    private fun checkUsername(newUsername: String) {

        disableViews()

        val query: Query = FirebaseDatabase.getInstance().reference.child("Users").orderByChild("username").equalTo(newUsername)
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.childrenCount > 0) {

                    FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {

                        override fun onDataChange(snapshot: DataSnapshot) {

                            val user: User? = snapshot.getValue(User::class.java)

                            val currentUsername: String = user!!.username!!

                            if (currentUsername == newUsername) {

                                updateUsername(currentUsername)

                            } else {

                                binding.mEditTextUsername.error = resources.getString(R.string.username_not_available_try_another)

                            }

                        }

                        override fun onCancelled(error: DatabaseError) {}

                    })

                } else {

                    updateUsername(newUsername)

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun updateUsername(username: String) {

        val hashMap = HashMap<String, Any>()
        hashMap["username"] = username

        FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.uid).updateChildren(hashMap)
        Toast.makeText(this, R.string.updated_information, Toast.LENGTH_SHORT).show()
        enableViews()

    }

    private fun enableViews() {

        binding.mEdit.isEnabled = true
        binding.mEditTextUsername.isEnabled = true
        binding.mSave.isEnabled = true
        binding.mProgressBar.visibility = View.GONE

    }

    private fun disableViews() {

        binding.mEdit.isEnabled = false
        binding.mEditTextUsername.isEnabled = false
        binding.mSave.isEnabled = false
        binding.mProgressBar.visibility = View.VISIBLE

    }

}