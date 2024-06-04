package com.example.plantaura2.data.repository

import com.example.plantaura2.domain.model.Recommendation

object Recommendations {
    val recommendations = listOf(
        Recommendation(
            parameterName = "Humedad Ambiente",
            highMessage = "Humedad alta, mueve la planta a un lugar más seco",
            lowMessage = "Humedad baja, mueve la planta a un lugar más húmedo"
        ),
        Recommendation(
            parameterName = "Humedad Suelo",
            highMessage = "Humedad del suelo alta, deja de regar la planta",
            lowMessage = "Humedad del suelo baja, riega la planta"
        ),
        Recommendation(
            parameterName = "Temperatura",
            highMessage = "Temperatura alta, mueve la planta a un lugar más fresco",
            lowMessage = "Temperatura baja, mueve la planta a un lugar más cálido"
        ),
        Recommendation(
            parameterName = "Luminosidad",
            highMessage = "Luminosidad alta, mueve la planta a un lugar con menos luz",
            lowMessage = "Luminosidad baja, mueve la planta a un lugar con más luz"
        )
    )
}