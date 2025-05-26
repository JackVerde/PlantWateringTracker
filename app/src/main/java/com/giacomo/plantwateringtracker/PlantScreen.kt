package com.giacomo.plantwateringtracker

import android.content.Context // Import Context for the copy function
import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch // For coroutine
import kotlinx.coroutines.withContext // For coroutine
import java.io.File // For File operations
import java.io.FileOutputStream // For File operations
import java.io.InputStream // For File operations
import java.util.UUID // To generate unique file names
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantScreen(plantId: Int, viewModel: PlantViewModel, navController: NavController) {
    val plant = viewModel.getPlantById(plantId)
    var plantName by remember { mutableStateOf(plant?.name ?: "") }
    var lastWateredDays by remember { mutableStateOf(plant?.lastWatered?.toString() ?: "") }
    // plantImageUri will now hold the URI STRING of the COPIED image in your app's internal storage
    var plantImageUri by remember { mutableStateOf(plant?.imageUri ?: "") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope() // For launching coroutines

    // Image Picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            coroutineScope.launch { // Perform file operations off the main thread
                val copiedImageUriString = saveImageToInternalStorage(context, sourceUri)
                if (copiedImageUriString != null) {
                    // If a previous image existed, delete it from internal storage
                    if (plantImageUri.isNotEmpty()) {
                        deleteImageFromInternalStorage(context, plantImageUri)
                    }
                    plantImageUri = copiedImageUriString
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error copying image", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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

                // Image Display
                if (plantImageUri.isNotEmpty()) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter( // Coil can load File URIs directly
                                ImageRequest.Builder(context)
                                    .data(Uri.parse(plantImageUri)) // URI of the copied file
                                    .crossfade(true)
                                    .error(android.R.drawable.stat_notify_error)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .build()
                            ),
                            contentDescription = "Plant Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text("No image selected", modifier = Modifier.padding(16.dp))
                }


                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Select Image")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextFieldWithLabel(
                    value = plantName,
                    onValueChange = { plantName = it },
                    label = "Edit Name"
                )

                Spacer(modifier = Modifier.height(12.dp))

                TextFieldWithLabel(
                    value = lastWateredDays,
                    onValueChange = { lastWateredDays = it },
                    label = "Edit Days Since Last Watered",
                    keyboardType = KeyboardType.Number
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                OutlinedButton(
                    onClick = {
                        // Delete the image from internal storage when deleting the plant
                        if (plantImageUri.isNotEmpty()) {
                            coroutineScope.launch { // Perform file operations off the main thread
                                deleteImageFromInternalStorage(context, plantImageUri)
                            }
                        }
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

// Helper function to save the image to internal storage
private suspend fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
    return withContext(Dispatchers.IO) { // Perform file I/O on a background thread
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            // Create a unique file name to avoid collisions
            val fileName = "plant_image_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName) // context.filesDir is app's private storage

            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            inputStream.close()
            Uri.fromFile(file).toString() // Return the URI string of the saved file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

// Helper function to delete an image from internal storage
private suspend fun deleteImageFromInternalStorage(context: Context, fileUriString: String) {
    withContext(Dispatchers.IO) {
        try {
            val fileUri = fileUriString.toUri()
            if (fileUri.scheme == "file") { // Ensure it's a file URI
                val filePath = fileUri.path
                if (filePath != null) {
                    val file = File(filePath)
                    if (file.exists() && file.isFile && file.canonicalPath.startsWith(context.filesDir.canonicalPath)) {
                        // Security check: ensure we are only deleting files within our app's directory
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}


@Composable
fun TextFieldWithLabel(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.tertiary,
            unfocusedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedLabelColor = MaterialTheme.colorScheme.tertiary,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.tertiary
        ),
        shape = MaterialTheme.shapes.medium
    )
}


@Preview(showBackground = true)
@Composable
fun PlantScreenPreview() {
    val context = LocalContext.current
    val viewModel = PlantViewModel(PlantRepository(context)) // Make sure PlantRepository is initialized
    val navController = rememberNavController()
    PlantScreen(plantId = 1, viewModel = viewModel, navController = navController)
}