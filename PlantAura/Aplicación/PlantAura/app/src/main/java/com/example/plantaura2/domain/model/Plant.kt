package com.example.plantaura2.domain.model

data class Plant(
    val id: String = "",
    val name: String = "",
    val plantType: String = "",
    val humedadSuelo: Float = 0f,
    val humedadAmbiente: Float = 0f,
    val temperaturaAmbiente: Float = 0f
)