package com.rasenyer.socialnetwork.models

data class Notification(

    val notificationUid: String? = "",

    val description: String? = "",

    val isPost: Boolean? = false,

    val postUid: String? = "",

    val postId: String? = ""

)
