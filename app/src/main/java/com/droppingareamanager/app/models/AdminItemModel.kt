package com.droppingareamanager.app.models

import com.google.firebase.database.Exclude

data class AdminItemModel(
    @get: Exclude
    var id: String? = null, // excluded for reference
    var sellerUid: String? ="",
    var sellerRefUid: String? ="",
    var sellerFullName: String? ="",
    var sellerShopName: String? = "",
    var buyerFullName: String? = "",
    var itemAmount: Any? = "",
    var itemHandlingFee: Any? = "",
    var status: String? = "",
    var dateClaimed: String? = "",
    var dateDropped: String? = "")
