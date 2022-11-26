package com.droppingareamanager.app.models

import com.google.firebase.database.Exclude

data class AdminCashoutModel(
    @get: Exclude
    var id: String? = null, // excluded for reference
    var uid: String? ="",
    var shopName: String? ="",
    var method: String? ="",
    var cashoutAmount: Any? = "",
    var status: String? = "",
    var time: Any? ="",
    var desc: String? = "")
