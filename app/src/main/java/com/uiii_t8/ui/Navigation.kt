package com.uiii_t8.ui

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.uiii_t8.ui.screens.AudioListScreen
import com.uiii_t8.ui.screens.ImageListScreen
import com.uiii_t8.ui.screens.RecordingScreen
import com.uiii_t8.ui.screens.VideoListScreen
import com.uiii_t8.ui.screens.VideoPlayerScreen
import com.uiii_t8.viewModel.MediaViewModel
import com.uiii_t8.viewModel.MediaViewModelFactory
import com.uiii_t8.viewModel.PlaybackViewModel
import com.uiii_t8.viewModel.PlaybackViewModelFactory

// Rutas de la navegacion
sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Recording : Screen("recording", "Grabar", Icons.Default.Mic)
    object AudioList : Screen("audio", "Audios", Icons.Default.AudioFile)
    object ImageList : Screen("images", "Imágenes", Icons.Default.Image)
    object VideoList : Screen("videos", "Videos", Icons.Default.Videocam)
    object VideoPlayer : Screen("video_player/{uri}", "Video Player", Icons.Default.Videocam) {
        fun createRoute(uri: String) = "video_player/$uri"
    }
}

// Lista para la barra de navegación inferior (excluye el reproductor)
val navBarItems = listOf(
    Screen.Recording,
    Screen.AudioList,
    Screen.ImageList,
    Screen.VideoList
)

// NAVEGACIÓN PRINCIPAL
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // Instancia de ViewModels usando las Factories
    val mediaViewModel: MediaViewModel = viewModel(
        factory = MediaViewModelFactory(application)
    )
    val playbackViewModel: PlaybackViewModel = viewModel(
        factory = PlaybackViewModelFactory(application)
    )

    Scaffold(
        bottomBar = {
            AppBottomNavBar(navController = navController)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Recording.route,
            modifier = Modifier.padding(padding)
        ) {
            // Pantalla 1: Grabación
            composable(Screen.Recording.route) {
                RecordingScreen(mediaViewModel = mediaViewModel)
            }

            // Pantalla 2: Lista de Audios
            composable(Screen.AudioList.route) {
                AudioListScreen(
                    mediaViewModel = mediaViewModel,
                    playbackViewModel = playbackViewModel
                )
            }

            // Pantalla 3: Imágenes
            composable(Screen.ImageList.route) {
                ImageListScreen(mediaViewModel = mediaViewModel)
            }

            // Pantalla 4: Lista de Videos
            composable(Screen.VideoList.route) {
                VideoListScreen(
                    mediaViewModel = mediaViewModel,
                    navController = navController
                )
            }

            // Pantalla 5: Reproductor (Recibe la URI del video)
            composable(
                route = Screen.VideoPlayer.route,
                arguments = listOf(navArgument("uri") { type = NavType.StringType })
            ) { backStackEntry ->
                val uriString = backStackEntry.arguments?.getString("uri")
                if (uriString != null) {
                    // Decodificamos y convertimos a Uri
                    val decodedUri = Uri.parse(Uri.decode(uriString))
                    VideoPlayerScreen(
                        uri = decodedUri,
                        playbackViewModel = playbackViewModel
                    )
                }
            }
        }
    }
}

// barrita de abajo
@Composable
fun AppBottomNavBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        navBarItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}