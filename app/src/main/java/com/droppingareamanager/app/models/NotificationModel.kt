package com.droppingareamanager.app.models

import java.io.Serializable
import java.time.LocalDate

data class NotificationModel(
    var text: String? = "",
    var title: String? = "",
    var receiver_id: String? = ""
)
