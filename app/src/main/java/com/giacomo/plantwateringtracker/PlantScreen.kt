package com.giacomo.plantwateringtracker

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantScreen(plantId: Int, viewModel: PlantViewModel, navController: NavController) {
    val plant = viewModel.getPlantById(plantId)
    var plantName by remember { mutableStateOf(plant?.name ?: "") }
    var lastWateredDays by remember { mutableStateOf(plant?.lastWatered?.toString() ?: "") }
    var plantImageUri by remember { mutableStateOf(plant?.imageUri ?: "") }

    val context = LocalContext.current

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            plantImageUri = it.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = plantName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(plantImageUri)

                // Image Display
                if (plantImageUri.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp), // Rounded but still squared
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth() // Full width with some padding
                            .aspectRatio(1f) // Keep it squared (1:1 aspect ratio)
                            .padding(horizontal = 16.dp) // Add padding on the sides
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(
                                ImageRequest.Builder(context)
                                    .data(plantImageUri)
                                    .crossfade(true) // Enable smooth transition
                                    .error(android.R.drawable.stat_notify_error) // Show an error icon if image fails
                                    .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder image while loading
                                    .build()
                            ),
                            contentDescription = "Plant Image",
                            contentScale = ContentScale.Crop, // Ensures image fills the container, cropping if needed
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                // Button to Pick Image
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Image")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Name Input
                TextFieldWithLabel(
                    value = plantName,
                    onValueChange = { plantName = it },
                    label = "Edit Name"
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Last Watered Input
                TextFieldWithLabel(
                    value = lastWateredDays,
                    onValueChange = { lastWateredDays = it },
                    label = "Edit Days Since Last Watered",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                Button(
                    onClick = {
                        val daysSinceWatered = lastWateredDays.toLongOrNull() ?: 0
                        if (plantName.isNotBlank()) {
                            viewModel.updatePlant(plantId, plantName, daysSinceWatered, plantImageUri)
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Changes")
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Delete Button
                OutlinedButton(
                    onClick = {
                        viewModel.deletePlant(plantId)
                        Toast.makeText(context, "Plant deleted", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Plant", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}



@Composable
fun TextFieldWithLabel(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text // Default to text type
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp), // Add vertical padding between fields for better spacing
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.tertiary
        ),
        shape = MaterialTheme.shapes.medium // Round the corners slightly
    )
}


@Preview(showBackground = true)
@Composable
fun PlantScreenPreview() {
    val viewModel = PlantViewModel(PlantRepository(LocalContext.current)) // Assuming a mock repository
    val navController = rememberNavController()

    // Use a fake plantId for preview purposes
    PlantScreen(plantId = 1, viewModel = viewModel, navController = navController)
}