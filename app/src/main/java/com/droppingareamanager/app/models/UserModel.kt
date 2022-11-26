package com.droppingareamanager.app.models

import com.google.firebase.database.Exclude

data class UserModel(
    @get: Exclude
    var id: String? = null, // excluded for reference
    val userId: Any? ="",
    val userType: Any? ="",
    val userEmail: String? = "",
    val userFullName: String? = "",
    val userShopName: String? = "",
    val userReferrer: String? = "",
    val userReferrerId: String? = "",
    val time: Any? ="",
    val balance: Any? ="",
    val cashout: Any? =""
)
