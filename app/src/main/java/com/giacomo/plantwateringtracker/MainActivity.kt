package com.giacomo.plantwateringtracker

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.giacomo.plantwateringtracker.ui.theme.PlantWateringTrackerTheme
import androidx.core.net.toUri


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlantWateringTrackerTheme {
                val viewModel: PlantViewModel = viewModel(factory = PlantViewModelFactory(applicationContext))
                PlantApp(viewModel)
            }
        }
    }
}

class PlantViewModelFactory(val context: Context) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return PlantViewModel(PlantRepository(context)) as T
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantApp(viewModel: PlantViewModel) {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        topBar = {
            if (currentDestination == "plant_list") { // Only show on main screen
                TopAppBar(
                    title = {
                        Text(
                            text = "My Plants",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        },
        bottomBar = {
            if (currentDestination == "plant_list"){
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center,
                        text = "Keep your plants hydrated!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

        },
        floatingActionButton = {
            if (currentDestination == "plant_list") {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Plant")
                }
            }
        }
    ) { innerPadding ->
        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {
                PlantDialog(viewModel = viewModel, onDismiss = { showDialog = false })
            }
        }
        NavHost(navController = navController, startDestination = "plant_list") {
            composable("plant_list") {
                PlantListScreen(
                    modifier = Modifier.padding(innerPadding),
                    viewModel = viewModel,
                    navController = navController
                )
            }
            composable("plant/{plantId}") { backStackEntry ->
                val plantId = backStackEntry.arguments?.getString("plantId")?.toIntOrNull()
                    ?: return@composable
                PlantScreen(
                    plantId = plantId,
                    viewModel = viewModel,
                    navController = navController
                )
            }
        }
    }
}

@Composable
fun PlantDialog(viewModel: PlantViewModel, onDismiss: () -> Unit) {
    var newPlantName by remember { mutableStateOf("") }
    var lastWateredDays by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surfaceVariant, // Use surfaceVariant to match app's background style
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title with matching color to the app theme
                Text(
                    text = "Add a New Plant",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface // Matches overall theme text color
                )

                // Plant Name Input
                OutlinedTextField(
                    value = newPlantName,
                    onValueChange = { newPlantName = it },
                    label = { Text("Plant Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Last Watered Input
                OutlinedTextField(
                    value = lastWateredDays,
                    onValueChange = { lastWateredDays = it },
                    label = { Text("Days Since Last Watered") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Action Buttons (Cancel and Add)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface // Matches app text color for Cancel button
                        )
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            // Convert days to milliseconds
                            val daysSinceWatered = lastWateredDays.toLongOrNull() ?: 0

                            if (newPlantName.isNotBlank()) {
                                viewModel.addPlant(newPlantName, daysSinceWatered)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary, // Use tertiary color for Add button
                            contentColor = MaterialTheme.colorScheme.onTertiary // Light text color on Add button
                        )
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@Composable
fun PlantListScreen(modifier: Modifier = Modifier, viewModel: PlantViewModel, navController: NavController) {
    val plants by viewModel.plants.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredPlants = plants.filter { it.name.contains(searchQuery, ignoreCase = true) }

    Column(modifier = modifier.padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)) { // Adjust overall padding if needed
        // Search Bar Card ... (remains the same)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp, start = 8.dp, end = 8.dp), // Adjust padding for search bar within the column
            shape = MaterialTheme.shapes.medium,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Plants") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            }
        }


        Spacer(modifier = Modifier.height(12.dp))

        // Plant Grid (Switched to LazyVerticalGrid)
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Example: 2 columns. Use GridCells.Adaptive for responsiveness.
            contentPadding = PaddingValues(8.dp), // Padding around the grid content itself
            verticalArrangement = Arrangement.spacedBy(8.dp),   // Spacing between rows
            horizontalArrangement = Arrangement.spacedBy(8.dp) // Spacing between columns
        ) {
            items(filteredPlants, key = { plant -> plant.id }) { plant -> // Added key for better performance
                // PlantItem will now be constrained by the grid cell.
                // You might need to adjust PlantItem's modifier if it uses fillMaxWidth
                // in a way that doesn't make sense for a grid cell.
                // For now, it should mostly work, but items might not be square.
                PlantItem(plant = plant, onWater = { viewModel.waterPlant(plant.id) }, onClick = {
                    navController.navigate("plant/${plant.id}")
                })
            }
        }
    }
}
@Composable
fun PlantItem(plant: Plant, onWater: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth() // Takes full width of the grid cell
            .aspectRatio(1f) // Makes the card square, good for images
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            // If there's an image, make card background transparent to show image through Box
            // Otherwise, use the default surfaceVariant color
            containerColor = if (plant.imageUri.isNullOrEmpty()) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                Color.Transparent // Will be covered by the image Box
            }
        )
    ) {
        Box( // Use Box to layer image and content
            modifier = Modifier.fillMaxSize(),
            // Align content to bottom-center if there's an image, otherwise center everything
            contentAlignment = if (plant.imageUri.isNullOrEmpty()) Alignment.Center else Alignment.BottomCenter
        ) {
            // Background Image (only if imageUri exists)
            if (!plant.imageUri.isNullOrEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(context)
                            .data(plant.imageUri.toUri()) // Parse the stored URI string
                            .crossfade(true)
                            .error(android.R.drawable.stat_notify_error) // Fallback for error
                            .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder while loading
                            .build()
                    ),
                    contentDescription = "Image of ${plant.name}",
                    contentScale = ContentScale.Crop, // Crop image to fill the card
                    modifier = Modifier.fillMaxSize()
                )
                // Scrim: Dark gradient overlay for better text readability on images
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f)),
                                startY = LocalContext.current.resources.displayMetrics.heightPixels * 0.5f, // Start gradient higher up
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )
            }

            // Original content (Text and Button)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (plant.imageUri.isNullOrEmpty()) 16.dp else 12.dp), // Less padding when image is present
                horizontalAlignment = Alignment.CenterHorizontally // Always center content horizontally
            ) {
                // Determine text color based on whether an image is present
                val textColor = if (plant.imageUri.isNullOrEmpty()) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    Color.White // Use white text on images
                }

                Text(
                    text = plant.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (plant.imageUri.isNullOrEmpty()) 8.dp else 4.dp))

                val lastWateredText = when (plant.lastWatered) {
                    0L -> "Today"
                    1L -> "Yesterday"
                    else -> "${plant.lastWatered} days ago"
                }
                Text(
                    text = lastWateredText,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (plant.imageUri.isNullOrEmpty()) textColor else textColor.copy(alpha = 0.85f), // Slightly dimmer for secondary text on image
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (plant.imageUri.isNullOrEmpty()) 12.dp else 8.dp))

                Button(
                    onClick = onWater,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary // This should be fine for both cases
                    )
                    // You might want to adjust button size or style if it's over an image
                ) {
                    Text("Water") // Text color on button is already handled by contentColor
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlantListPreview() {
    val fakeViewModel = PlantViewModel(PlantRepository(LocalContext.current))
    PlantWateringTrackerTheme {
        PlantApp(fakeViewModel)
    }
}