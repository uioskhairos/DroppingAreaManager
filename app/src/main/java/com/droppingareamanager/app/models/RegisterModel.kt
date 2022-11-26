package com.droppingareamanager.app.models


data class RegisterModel(
    val userId: Any? ="",
    val userEmail: String? = "",
    val userFullName: String? = "",
    val userShopName: String? = "",
    val userReferrer: String? = "",
    val userReferrerId: String? = "",
    val time: String = "",
    val accountType: String = "") {

}