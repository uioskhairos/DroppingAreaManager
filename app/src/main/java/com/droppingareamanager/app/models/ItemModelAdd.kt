package com.droppingareamanager.app.models

data class ItemModelAdd(
    var sellerUid: String? ="",
    var sellerFullName: String? ="",
    var sellerShopName: String? = "",
    var sellerRefUid: String? ="",
    var buyerFullName: String? = "",
    var itemAmount: Any? = ""
)
