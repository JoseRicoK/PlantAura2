package com.example.plantaura2.domain.model

data class MeasurementData(
    val timestamp: String = "",
    val humedadAmbiente: Int = 0,
    val humedadSuelo: Int = 0,
    val temperatura: Float = 0f
)
