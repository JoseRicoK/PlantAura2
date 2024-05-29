package com.example.plantaura2.domain.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Plant(
    val id: String = "",
    val name: String = ""
)
