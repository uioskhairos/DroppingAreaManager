package com.droppingareamanager.app.models

import java.io.Serializable
import java.time.LocalDate

data class SalesModel(
    var date: String? = "",
    var total: String? = ""
) : Serializable
