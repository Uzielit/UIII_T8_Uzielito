package com.uiii_t8.ui.screens

//Una lista con miniaturas de videos
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.createRoute
import coil.compose.AsyncImage
import com.uiii_t8.formatDate
import com.uiii_t8.formatDuration
import com.uiii_t8.ui.screens.*
import com.uiii_t8.ui.Screen
import com.uiii_t8.data.MediaItem
import com.uiii_t8.viewModel.MediaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoListScreen(
    mediaViewModel: MediaViewModel,
    navController: NavController
) {
    val videoList by mediaViewModel.allVideos.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Mis Videos") })
        }
    ) { padding ->
        if (videoList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No has grabado videos aún.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(videoList) { item ->
                    VideoCard(
                        item = com.uiii_t8.data.MediaItem(
                            id = item.id, uri = item.uri, name = item.name, date = item.date, duration = item.duration, type = item.type
                        ),
                        onClick = {
                            // Navegar al reproductor codificando la URI
                            val encodedUri = Uri.encode(item.uri)
                          //  navController.navigate(Screen.VideoPlayer.createRoute(encodedUri))
                            navController.navigate(Screen.VideoPlayer.createRoute(encodedUri))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VideoCard(item: com.uiii_t8.data.MediaItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                AsyncImage(
                    model = item.uri,
                    contentDescription = "Miniatura",
                    modifier = Modifier
                        .size(100.dp, 70.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black),
                    contentScale = ContentScale.Crop
                )
                Icon(
                    imageVector = Icons.Default.PlayCircleOutline,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDuration(item.duration),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = formatDate(item.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}