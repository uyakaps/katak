package com.rasenyer.socialnetwork.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.databinding.FragmentRegisterBinding
import com.rasenyer.socialnetwork.ui.activities.SecondActivity

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onRegisterClick()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onRegisterClick() {

        binding.mRegister.setOnClickListener {

            when {

                TextUtils.isEmpty(binding.mEditTextUsername.text.toString()) -> {
                    binding.mEditTextUsername.error = resources.getString(R.string.enter_a_username)
                }

                TextUtils.isEmpty(binding.mEditTextEmail.text.toString()) -> {
                    binding.mEditTextEmail.error = resources.getString(R.string.enter_an_email)
                }

                !Patterns.EMAIL_ADDRESS.matcher(binding.mEditTextEmail.text.toString()).matches() -> {
                    binding.mEditTextEmail.error = resources.getString(R.string.invalid_email_please_enter_a_valid_email)
                }

                TextUtils.isEmpty(binding.mEditTextPassword.text.toString()) -> {
                    binding.mEditTextPassword.error = resources.getString(R.string.please_enter_a_password)
                }

                binding.mEditTextPassword.text.toString().length < 8 -> {
                    binding.mEditTextPassword.error = resources.getString(R.string.password_must_be_8_or_more_characters)
                }

                else -> {

                    checkUsername()

                }

            }

        }

    }

    private fun checkUsername() {

        disableViews()

        val query: Query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username").equalTo(binding.mEditTextUsername.text.toString())
        query.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.childrenCount > 0) {

                    binding.mEditTextUsername.error = resources.getString(R.string.username_not_available_try_another)
                    enableViews()

                } else {

                    createUser(binding.mEditTextUsername.text.toString(), binding.mEditTextEmail.text.toString(), binding.mEditTextPassword.text.toString())

                }

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun createUser(username: String, email: String, password: String) {

        val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {

                saveUserInfo(username)

            } else {

                val message = it.exception!!.toString()
                binding.mEditTextEmail.error = "Error: $message"
                enableViews()

            }

        }

    }

    private fun saveUserInfo(username: String) {

        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val hashMap = HashMap<String, Any>()
        hashMap["uid"] = FirebaseAuth.getInstance().currentUser!!.uid
        hashMap["username"] = username

        databaseReference.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(hashMap).addOnCompleteListener {

            if (it.isSuccessful) {

                startActivity(Intent(context, SecondActivity::class.java))
                Toast.makeText(activity, R.string.registered_user_successfully, Toast.LENGTH_SHORT).show()

            } else {

                val message = it.exception!!.toString()
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                enableViews()

            }

        }

    }

    private fun enableViews() {

        binding.mEditTextUsername.isEnabled = true
        binding.mEditTextEmail.isEnabled = true
        binding.mEditTextPassword.isEnabled = true
        binding.mRegister.isEnabled = true
        binding.mProgressBar.visibility = View.GONE

    }

    private fun disableViews() {

        binding.mEditTextUsername.isEnabled = false
        binding.mEditTextEmail.isEnabled = false
        binding.mEditTextPassword.isEnabled = false
        binding.mRegister.isEnabled = false
        binding.mProgressBar.visibility = View.VISIBLE

    }

}