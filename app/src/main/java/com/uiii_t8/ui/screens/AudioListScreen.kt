package com.uiii_t8.ui.screens

//Una lista vertical que muestra los audios grabados y permite reproducirlos.
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uiii_t8.data.MediaItem
import com.uiii_t8.formatDate
import com.uiii_t8.formatDuration
import com.uiii_t8.viewModel.MediaViewModel
import com.uiii_t8.viewModel.PlaybackViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioListScreen(
    mediaViewModel: MediaViewModel,
    playbackViewModel: PlaybackViewModel
) {
    val audioList by mediaViewModel.allAudio.collectAsState()
    val isPlaying by playbackViewModel.isPlaying.collectAsState()
    val isAccelerometerOn by playbackViewModel.isAccelerometerEnabled.collectAsState()
    val currentVolume by playbackViewModel.currentVolume.collectAsState()

    var currentlyPlayingUri by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Mis Audios") },
                actions = {
                    Text(
                        text = "Vol: ${(currentVolume * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    IconButton(onClick = { playbackViewModel.toggleAccelerometer() }) {
                        Icon(
                            if (isAccelerometerOn) Icons.Default.Sensors else Icons.Default.SensorsOff,
                            contentDescription = "Control por Movimiento",
                            tint = if (isAccelerometerOn) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (audioList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay grabaciones de audio aún.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(audioList) { item ->
                    AudioCard(
                        item = item,
                        isPlaying = isPlaying && currentlyPlayingUri == item.uri,
                        onPlayClick = {
                            if (currentlyPlayingUri == item.uri) {
                                //  pausamos/reanudamos
                                playbackViewModel.togglePlayPause()
                            } else {

                                playbackViewModel.playMedia(item.uri)
                                currentlyPlayingUri = item.uri
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AudioCard(
    item: MediaItem,
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono Izquierdo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AudioFile,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Textos (Nombre y Fecha)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatDuration(item.duration)} • ${formatDate(item.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Botón Play/Pause Flotante
            IconButton(
                onClick = onPlayClick,
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}