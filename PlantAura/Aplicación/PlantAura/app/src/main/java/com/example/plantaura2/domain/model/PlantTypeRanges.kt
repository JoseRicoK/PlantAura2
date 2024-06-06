package com.example.plantaura2.domain.model

data class PlantTypeRanges(
    val humedadAmbienteMin: Int?,
    val humedadAmbienteMax: Int?,
    val humedadSueloMin: Int?,
    val humedadSueloMax: Int?,
    val temperaturaAmbienteMin: Double?,
    val temperaturaAmbienteMax: Double?,
    val luzMin: Int?,
    val luzMax: Int?,
    val conductividadMin: Int?,
    val conductividadMax: Int?,
    val phMin: Double?,
    val phMax: Double?,
    val nitrogenoMin: Int?,
    val nitrogenoMax: Int?,
    val fosforoMin: Int?,
    val fosforoMax: Int?,
    val potasioMin: Int?,
    val potasioMax: Int?,
    val salinidadMin: Int?,
    val salinidadMax: Int?,
    val tdsMin: Int?,
    val tdsMax: Int?
) {
    // Constructor sin argumentos
    constructor() : this(
        humedadAmbienteMin = null,
        humedadAmbienteMax = null,
        humedadSueloMin = null,
        humedadSueloMax = null,
        temperaturaAmbienteMin = null,
        temperaturaAmbienteMax = null,
        luzMin = null,
        luzMax = null,
        conductividadMin = null,
        conductividadMax = null,
        phMin = null,
        phMax = null,
        nitrogenoMin = null,
        nitrogenoMax = null,
        fosforoMin = null,
        fosforoMax = null,
        potasioMin = null,
        potasioMax = null,
        salinidadMin = null,
        salinidadMax = null,
        tdsMin = null,
        tdsMax = null
    )
}
