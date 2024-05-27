package com.example.plantaura2.domain.model

data class PlantType(
    val nombreCientifico: String = "",
    val nombreComun: String = "",
    val humedadAmbienteMax: Float = 0f,
    val humedadAmbienteMin: Float = 0f,
    val humedadSueloMax: Float = 0f,
    val humedadSueloMin: Float = 0f,
    val luzMax: Float = 0f,
    val luzMin: Float = 0f,
    val temperaturaAmbienteMax: Float = 0f,
    val temperaturaAmbienteMin: Float = 0f
)