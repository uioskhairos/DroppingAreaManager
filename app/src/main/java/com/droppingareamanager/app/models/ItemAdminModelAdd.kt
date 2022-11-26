package com.droppingareamanager.app.models

data class ItemAdminModelAdd(
    var sellerUid: String? ="",
    var sellerFullName: String? ="",
    var sellerShopName: String? = "",
    var sellerRefUid: String? ="",
    var buyerFullName: String? = "",
    var itemAmount: Any? = "",
    var itemHandlingFee: Any? = "",
    var status: String? = "",
    var dateClaimed: String? = "",
    var dateDropped: String? = "",
    var cashOutStatus: String? = ""
)
