package com.uiii_t8.ui.screens

//La pantalla negra de pantalla completa donde se reproduce el video seleccionado
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.ui.PlayerView
import com.uiii_t8.viewModel.PlaybackViewModel

@Composable
fun VideoPlayerScreen(
    uri: Uri,
    playbackViewModel: PlaybackViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(uri) {
        playbackViewModel.playMedia(uri.toString())
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> playbackViewModel.exoPlayer.pause()
                Lifecycle.Event.ON_RESUME -> playbackViewModel.exoPlayer.play()
                // No hacemos release aquí lo maneja el ViewModel al destruirse
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            playbackViewModel.exoPlayer.pause()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = playbackViewModel.exoPlayer
                    useController = true // Barra de progesos
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}