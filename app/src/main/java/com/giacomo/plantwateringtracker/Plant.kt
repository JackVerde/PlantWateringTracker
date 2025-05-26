package com.giacomo.plantwateringtracker

data class Plant(
    val id: Int,
    val name: String,
    val lastWatered: Long,
    val imageUri: String? = null,
    val type: PlantType? = PlantType.NONE
)

enum class PlantType {
    NONE,
    OUTDOOR,
    INDOOR
}