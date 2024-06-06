package com.example.plantaura2.domain.model

data class MeasurementData(
    val timestamp: String = "",
    val humedadAmbiente: Int = 0,
    val humedadSuelo: Int = 0,
    val temperatura: Float = 0f,
    val luminosidad: Float = 0f,
    val conductividad: Int? = null,
    val ph: Float? = null,
    val nitrogeno: Int? = null,
    val fosforo: Int? = null,
    val potasio: Int? = null,
    val salinidad: Int? = null,
    val tds: Int? = null
)
