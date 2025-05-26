package com.giacomo.plantwateringtracker

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlantRepository(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("plant_preferences", Context.MODE_PRIVATE)

    private val gson = Gson()

    private val _plants = MutableStateFlow<List<Plant>>(loadPlants()) // Initialize with data from SharedPreferences
    val plants: StateFlow<List<Plant>> = _plants // StateFlow for observing plant changes



    // Load plants from SharedPreferences and order by lastWatered (ascending)
    private fun loadPlants(): List<Plant> {
        val json = sharedPreferences.getString("plants", null)
        return if (json != null) {
            val type = object : TypeToken<List<Plant>>() {}.type
            val plantsList = gson.fromJson<List<Plant>>(json, type) ?: emptyList()
            plantsList.sortedBy { it.lastWatered }
        } else {
            emptyList() // Return empty list if no data found
        }
    }

    // Save plants to SharedPreferences
    private fun savePlants(plants: List<Plant>) {
        val json = gson.toJson(plants)
        sharedPreferences.edit { putString("plants", json) }
    }

    // Add a new plant, with an optional image URI
    fun addPlant(name: String, lastWatered: Long, imageUri: String? = null) {
        val newPlant = Plant(
            id = (_plants.value.maxOfOrNull { it.id } ?: 0) + 1,
            name = name,
            lastWatered = lastWatered,
            imageUri = imageUri,
            //type = PlantType.NONE
        )
        val updatedPlants = _plants.value + newPlant
        _plants.value = updatedPlants.sortedBy { it.lastWatered }
        savePlants(_plants.value)
    }

    // Update a plant's details, including optional image updates
    fun updatePlant(id: Int, name: String, lastWatered: Long, imageUri: String? = null) {
        val updatedPlants = _plants.value.map { plant ->
            if (plant.id == id) {
                plant.copy(
                    name = name,
                    lastWatered = lastWatered,
                    imageUri = imageUri ?: plant.imageUri // Preserve existing image if none provided
                )
            } else plant
        }.sortedBy { it.lastWatered }
        _plants.value = updatedPlants
        savePlants(_plants.value)
    }

    // Delete a plant by ID
    fun deletePlant(id: Int) {
        val updatedPlants = _plants.value.filterNot { plant -> plant.id == id }
        _plants.value = updatedPlants.sortedBy { it.lastWatered }
        savePlants(updatedPlants)
    }
}