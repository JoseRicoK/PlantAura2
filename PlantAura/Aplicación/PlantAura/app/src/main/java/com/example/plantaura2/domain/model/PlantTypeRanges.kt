package com.example.plantaura2.domain.model

data class PlantTypeRanges(
    val humedadAmbienteMax: Int = 0,
    val humedadAmbienteMin: Int = 0,
    val humedadSueloMax: Int = 0,
    val humedadSueloMin: Int = 0,
    val temperaturaAmbienteMax: Int = 0,
    val temperaturaAmbienteMin: Int = 0,
    val luzMax: Int = 0,
    val luzMin: Int = 0
)
