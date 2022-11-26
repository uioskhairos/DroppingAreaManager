package com.droppingareamanager.app.models

data class CashoutUserModel(
    val userId: Any? ="",
    val userType: Any? ="",
    val userEmail: String? = "",
    val userFullName: String? = "",
    val userShopName: String? = "",
    val userReferrer: String? = "",
    val userReferrerId: String? = "",
    val time: Any? ="",
    val balance: String? ="",
    val cashout: String? =""
)
