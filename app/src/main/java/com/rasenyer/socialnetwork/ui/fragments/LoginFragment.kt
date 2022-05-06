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
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.rasenyer.socialnetwork.R
import com.rasenyer.socialnetwork.databinding.FragmentLoginBinding
import com.rasenyer.socialnetwork.ui.activities.SecondActivity

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        onLoginClick()
        onRegisterClick()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onLoginClick() {

        binding.mLogin.setOnClickListener {

            when {

                TextUtils.isEmpty(binding.mEditTextEmail.text.toString()) -> {
                    binding.mEditTextEmail.error = resources.getString(R.string.enter_an_email)
                }

                !Patterns.EMAIL_ADDRESS.matcher(binding.mEditTextEmail.text.toString()).matches() -> {
                    binding.mEditTextEmail.error = resources.getString(R.string.invalid_email_please_enter_a_valid_email)
                }

                TextUtils.isEmpty(binding.mEditTextPassword.text.toString()) -> {
                    binding.mEditTextPassword.error = resources.getString(R.string.please_enter_a_password)
                }

                else -> {
                    login(binding.mEditTextEmail.text.toString(), binding.mEditTextPassword.text.toString())
                }

            }

        }

    }

    private fun onRegisterClick() {

        binding.mRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

    }

    private fun login(email: String, password: String) {

        disableViews()

        val firebaseAuth : FirebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {

                startActivity(Intent(activity, SecondActivity::class.java))
                Toast.makeText(activity, R.string.successful_login, Toast.LENGTH_SHORT).show()

            } else {

                val message = it.exception!!.toString()
                Toast.makeText(activity, "Error: $message", Toast.LENGTH_LONG).show()
                enableViews()

            }

        }

    }

    private fun enableViews() {

        binding.mEditTextEmail.isEnabled = true
        binding.mEditTextPassword.isEnabled = true
        binding.mLogin.isEnabled = true
        binding.mRegister.isEnabled = true
        binding.mProgressBar.visibility = View.GONE

    }

    private fun disableViews() {

        binding.mEditTextEmail.isEnabled = false
        binding.mEditTextPassword.isEnabled = false
        binding.mLogin.isEnabled = false
        binding.mRegister.isEnabled = false
        binding.mProgressBar.visibility = View.VISIBLE

    }

}