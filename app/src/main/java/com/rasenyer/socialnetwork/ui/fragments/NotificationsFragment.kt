package com.rasenyer.socialnetwork.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.rasenyer.socialnetwork.adapters.NotificationAdapter
import com.rasenyer.socialnetwork.databinding.FragmentNotificationsBinding
import com.rasenyer.socialnetwork.models.Notification
import com.rasenyer.socialnetwork.ui.activities.CommentActivity
import com.rasenyer.socialnetwork.ui.activities.UserActivity
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_ID
import com.rasenyer.socialnetwork.util.Constants.Companion.POST_UID
import com.rasenyer.socialnetwork.util.Constants.Companion.USER_UID

class NotificationsFragment : Fragment() {

    private lateinit var firebaseUser: FirebaseUser
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var notifications: MutableList<Notification>

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseUser= FirebaseAuth.getInstance().currentUser!!
        notificationAdapter = NotificationAdapter()
        notifications = ArrayList()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setRecyclerView()
        setNotifications()
        setOnItemClick()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setRecyclerView() {

        binding.mRecyclerView.apply {
            adapter = notificationAdapter
        }

    }

    private fun setNotifications() {

        val databaseReference = FirebaseDatabase.getInstance().getReference("Notifications").child(firebaseUser.uid)
        databaseReference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                notifications.clear()

                for (snapshot in dataSnapshot.children) {

                    val notification: Notification? = snapshot.getValue(Notification::class.java)

                    notifications.add(notification!!)

                }

                notifications.reverse()
                notificationAdapter.setNotifications(notifications)

            }

            override fun onCancelled(error: DatabaseError) {}

        })

    }

    private fun setOnItemClick() {

        notificationAdapter.setOnItemClick(object : NotificationAdapter.OnItemClick {

            override fun onClick(notification: Notification) {

                if (notification.description.equals("Started following you")) {

                    val intent = Intent(activity, UserActivity::class.java)
                    intent.putExtra(USER_UID, notification.notificationUid)
                    startActivity(intent)

                } else {

                    val intent = Intent(activity, CommentActivity::class.java)
                    intent.putExtra(POST_UID, notification.postUid)
                    intent.putExtra(POST_ID, notification.postId)
                    startActivity(intent)

                }

            }

        })

    }

}