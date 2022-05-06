package com.rasenyer.socialnetwork.ui.fragments

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import coil.load
import com.github.drjacky.imagepicker.ImagePicker
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.databinding.FragmentAddBinding

class AddFragment : Fragment() {

    private lateinit var firebaseUser: FirebaseUser
    private var imageUri: Uri? = null

    private var _binding: FragmentAddBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseUser = FirebaseAuth.getInstance().currentUser!!

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setOnImagePostClick()
        setOnPostClick()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setOnImagePostClick() {

        binding.mImagePost.setOnClickListener {
            ImagePicker.with(context as Activity).crop().createIntentFromDialog {
                launcherImage.launch(it)
            }
        }

    }

    private val launcherImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        if (it.resultCode == Activity.RESULT_OK) {

            imageUri = it.data?.data!!

            binding.mImagePost.load(imageUri) {
                placeholder(R.color.purple_200)
                error(R.color.purple_200)
                crossfade(true)
                crossfade(400)
            }

        }

    }

    private fun setOnPostClick() {

        binding.mPost.setOnClickListener {

            when {

                TextUtils.isEmpty(binding.mEditTextDescription.text.toString()) -> {
                    binding.mEditTextDescription.error = resources.getString(R.string.enter_a_description)
                }

                imageUri == null -> {
                    Toast.makeText(activity, R.string.select_an_image, Toast.LENGTH_SHORT).show()
                }

                else -> {
                    addPost()
                }

            }

        }

    }

    private fun addPost() {

        disableViews()

        val storageReference = FirebaseStorage.getInstance().reference.child("PostImages").child(System.currentTimeMillis().toString() + ".jpg")

        val storageTask: StorageTask<*>
        storageTask = storageReference.putFile(imageUri!!)

        storageTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->

            if (task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }

            return@Continuation storageReference.downloadUrl

        }).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                val databaseReference = FirebaseDatabase.getInstance().getReference("Posts")
                val id = databaseReference.push().key

                val downloadUrl = task.result
                val imageUrl = downloadUrl.toString()

                val hashMap = HashMap<String, Any>()
                hashMap["uid"] = firebaseUser.uid
                hashMap["id"] = id!!
                hashMap["description"] = binding.mEditTextDescription.text.toString()
                hashMap["image"] = imageUrl
                hashMap["date"] = System.currentTimeMillis().toString()

                databaseReference.child(id).setValue(hashMap)

                enableViews()
                clear()
                Toast.makeText(activity, R.string.published_post, Toast.LENGTH_SHORT).show()

            }

        }


    }

    private fun enableViews() {

        binding.mEditTextDescription.isEnabled = true
        binding.mImagePost.isEnabled = true
        binding.mPost.isEnabled = true
        binding.mProgressBar.visibility = View.GONE

    }

    private fun disableViews() {

        binding.mEditTextDescription.isEnabled = false
        binding.mImagePost.isEnabled = false
        binding.mPost.isEnabled = false
        binding.mProgressBar.visibility = View.VISIBLE

    }

    private fun clear() {

        binding.mEditTextDescription.setText("")
        binding.mImagePost.setImageResource(R.drawable.ic_add)
        imageUri = null

    }

}