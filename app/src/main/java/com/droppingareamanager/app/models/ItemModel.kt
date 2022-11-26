package com.droppingareamanager.app.models

data class ItemModel(
    var sellerUid: String? ="",
    var sellerFullName: String? ="",
    var sellerShopName: String? = "",
    var sellerRefUid: String? ="",
    var buyerFullName: String? = "",
    var itemAmount: Any? = "",
    var itemHandlingFee: Any? = "",
    var dateClaimed: String? = "",
    var dateDropped: String? = "",
    var cashOutStatus: String? = "",
    var status: String? = ""
)
