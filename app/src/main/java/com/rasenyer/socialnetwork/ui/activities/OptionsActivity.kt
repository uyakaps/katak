package com.rasenyer.socialnetwork.ui.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.rasenyer.socialnetwork.databinding.ActivityOptionsBinding

class OptionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding= ActivityOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onEditProfileClick()
        onSavedPostsClick()
        onLogoutClick()

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    private fun onEditProfileClick() {

        binding.mEditProfile.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }

    }

    private fun onSavedPostsClick() {

        binding.mSavedPosts.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

    }

    private fun onLogoutClick() {

        binding.mLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, FirstActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }

    }

}