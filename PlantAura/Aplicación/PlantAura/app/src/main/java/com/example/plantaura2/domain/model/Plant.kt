package com.example.plantaura2.domain.model

data class Plant(
    val id: String = "",
    val name: String = "",
    val plantType: String = "",
    val sensorType: String = "",
    val revive: Boolean = false,
    val recommendations: List<String> = emptyList(),
    val hiddenRecommendations: List<Int> = emptyList()
)