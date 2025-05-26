# Plant Watering Tracker 🪴💧

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Keep your leafy friends happy and hydrated! Plant Watering Tracker is a simple Android application built with Jetpack Compose to help you remember when to water your plants.

## ✨ Features

*   **Plant List:** View all your plants at a glance in a clean, modern grid layout.
*   **Add New Plants:** Easily add new plants with their name, an optional image, and the last time they were watered.
*   **Track Watering:** Mark when a plant has been watered. The app calculates and displays how many days ago it was last watered.
*   **Plant Details & Editing:** Tap on a plant to view and edit its details, including:
    *   Name
    *   Last watered date
    *   Plant Image (select from device gallery)
*   **Search Functionality:** Quickly find specific plants in your collection by name.
*   **Visual Appeal:** Plant items in the grid feature their chosen image as a background with a text overlay.
*   **Smooth Transitions:** Enjoy pleasant animations when navigating between the list and detail screens.
*   **Data Persistence:** Your plant data, including image URIs, is saved locally on your device using a Room Database.

## 🛠️ Built With

*   **[Kotlin](https://kotlinlang.org/)**: Primary programming language.
*   **[Jetpack Compose](https://developer.android.com/jetpack/compose)**: Android’s modern toolkit for building native UI.
    *   **Material 3**: For UI components and styling.
    *   **Navigation Compose**: For handling in-app navigation and screen transitions.
    *   **ViewModel**: To store and manage UI-related data in a lifecycle-conscious way.
    *   **StateFlow & Coroutines**: For observable data and asynchronous operations.
*   **[Coil](https://coil-kt.github.io/coil/)**: For efficient image loading and display.
*   **[Room Database](https://developer.android.com/training/data-storage/room)**: For local data storage, ensuring your plant list persists.
*   **Coroutines**: For managing background threads and asynchronous tasks smoothly.

## 🚀 Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

*   Android Studio Hedgehog (or newer recommended)
*   Android SDK API 26 (Oreo) or higher
*   Kotlin 1.9.0 (or as specified in the project's `build.gradle`)

## 📝 How to Use

1.  Launch the app on your device or emulator.
2.  The main screen displays your list of plants. If it's your first time, it will be empty.
3.  Tap the **'+' (Add)** button in the bottom right corner to add a new plant.
4.  In the "Add Plant" dialog, enter the plant's name and the number of days since it was last watered.
5.  To add an image for a plant:
    *   When adding a new plant, you'll see a placeholder.
    *   When viewing/editing an existing plant's details, tap the image area.
    *   This will open the device's image gallery for you to select a picture.
6.  Your plants will appear in a grid on the main screen, each with its image (if provided) and name.
7.  Tap on any plant in the grid to go to its details screen. Here you can:
    *   Update its name or last watered day count.
    *   Change its image.
    *   Mark it as "Watered Today".
    *   Delete the plant.
8.  Use the search bar at the top of the main screen to filter your plant list by name.

## 🤝 Contributing

Contributions are welcome! If you have ideas for improvements or find any bugs, feel free to:

1.  Fork the Project
2.  Create your Feature Branch (`git checkout -b feature/MyNewFeature`)
3.  Commit your Changes (`git commit -m 'Add some MyNewFeature'`)
4.  Push to the Branch (`git push origin feature/MyNewFeature`)
5.  Open a Pull Request

You can also open an issue to discuss potential changes or report bugs.

## 📜 License

Distributed under the MIT License. See `LICENSE` file for more information.
