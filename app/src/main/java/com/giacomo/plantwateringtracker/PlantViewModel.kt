package com.giacomo.plantwateringtracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map

class PlantViewModel(private val repository: PlantRepository) : ViewModel() {

    // Using the reactive flow directly without re-sorting every time
    val plants: StateFlow<List<Plant>> = repository.plants
        .map { plantsList ->
            plantsList.map { plant ->
                plant.copy(lastWatered = convertTimestampToDays(plant.lastWatered))
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Water a plant by updating the 'lastWatered' timestamp
    fun waterPlant(id: Int) {
        val plant = getPlantById(id)
        if (plant != null) {
            repository.updatePlant(id, plant.name, convertDaysToTimestamp("0"), plant.imageUri)
        }
    }

    // Add a new plant with an optional image
    fun addPlant(name: String, lastWateredDays: Long = 0, imageUri: String? = null) {
        repository.addPlant(name, convertDaysToTimestamp(lastWateredDays.toString()), imageUri)
    }

    // Update a plant's details (name, lastWatered timestamp, and image URI)
    fun updatePlant(id: Int, name: String, lastWateredDays: Long, imageUri: String? = null) {
        repository.updatePlant(id, name, convertDaysToTimestamp(lastWateredDays.toString()), imageUri)
    }

    // Update only the image of a plant
    fun updatePlantImage(id: Int, imageUri: String) {
        val plant = getPlantById(id)
        if (plant != null) {
            repository.updatePlant(id, plant.name, plant.lastWatered, imageUri)
        }
    }

    // Get a plant by its ID
    fun getPlantById(id: Int): Plant? {
        return plants.value.find { it.id == id }
    }

    // Delete a plant by ID
    fun deletePlant(id: Int) {
        repository.deletePlant(id)
    }

    // Helper function to convert days to timestamp
    private fun convertDaysToTimestamp(lastWateredDays: String?): Long {
        val daysSinceWatered = lastWateredDays?.toLongOrNull() ?: 0
        return System.currentTimeMillis() - (daysSinceWatered * 24 * 60 * 60 * 1000L)
    }

    // Helper function to convert timestamp to days since last watered
    private fun convertTimestampToDays(lastWateredTimestamp: Long): Long {
        return (System.currentTimeMillis() - lastWateredTimestamp) / (24 * 60 * 60 * 1000)
    }
}