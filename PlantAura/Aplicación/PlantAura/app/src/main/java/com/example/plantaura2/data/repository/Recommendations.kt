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
        ),
        Recommendation(
            parameterName = "Conductividad",
            highMessage = "Conductividad alta, reduce la cantidad de fertilizantes",
            lowMessage = "Conductividad baja, aumenta la cantidad de fertilizantes"
        ),
        Recommendation(
            parameterName = "pH",
            highMessage = "pH alto, añade más compost o turba",
            lowMessage = "pH bajo, añade más cal al suelo"
        ),
        Recommendation(
            parameterName = "Nitrógeno",
            highMessage = "Nitrógeno alto, reduce la cantidad de fertilizantes",
            lowMessage = "Nitrógeno bajo, añade más fertilizante nitrogenado"
        ),
        Recommendation(
            parameterName = "Fósforo",
            highMessage = "Fósforo alto, reduce la cantidad de fertilizantes",
            lowMessage = "Fósforo bajo, añade más fertilizante fosforado"
        ),
        Recommendation(
            parameterName = "Potasio",
            highMessage = "Potasio alto, reduce la cantidad de fertilizantes",
            lowMessage = "Potasio bajo, añade más fertilizante potásico"
        ),
        Recommendation(
            parameterName = "Salinidad",
            highMessage = "Salinidad alta, riega más para diluir las sales",
            lowMessage = "Salinidad baja, añade fertilizante que contenga sales necesarias"
        ),
        Recommendation(
            parameterName = "TDS",
            highMessage = "TDS alto, reduce la cantidad de nutrientes en el agua",
            lowMessage = "TDS bajo, añade más nutrientes al agua"
        )
    )
}
