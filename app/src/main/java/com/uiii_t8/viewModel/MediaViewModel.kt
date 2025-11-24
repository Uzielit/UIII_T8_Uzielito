package com.uiii_t8.viewModel

//Se encarga de hablar con la base de datos (guardar fotos, listar videos, etc.)

import android.app.Application
import android.content.ContentResolver
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uiii_t8.data.AppDatabase
import com.uiii_t8.data.MediaItem
import com.uiii_t8.data.MediaType
import com.uiii_t8.data.repository.MediaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class MediaViewModel(application: Application, private val repository: MediaRepository) : AndroidViewModel(application) {

    // Listas observables para la UI
    val allAudio: StateFlow<List<MediaItem>> = repository.getAllAudio()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allImages: StateFlow<List<MediaItem>> = repository.getAllImages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVideos: StateFlow<List<MediaItem>> = repository.getAllVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Función para insertar desde una URI (Cámara)
    fun insertMediaFromUri(uri: Uri, type: MediaType) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val metadata = getMetadataFromUri(context.contentResolver, uri)
                val item = MediaItem(
                    uri = uri.toString(),
                    name = metadata.first,
                    date = System.currentTimeMillis(),
                    duration = metadata.second,
                    type = type
                )
                repository.insertMedia(item)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Función para insertar desde un Archivo (Audio)
    fun insertMediaFromFile(file: File, type: MediaType) {
        viewModelScope.launch {
            try {
                val context = getApplication<Application>().applicationContext
                val authority = "${context.packageName}.fileprovider"
                val uri = FileProvider.getUriForFile(context, authority, file)
                insertMediaFromUri(uri, type)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Extraer nombre y duración
    private fun getMetadataFromUri(contentResolver: ContentResolver, uri: Uri): Pair<String, Long> {
        var aName = "Desconocido"
        var aDuration = 0L

        // Nombre
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    aName = cursor.getString(nameIndex)
                }
            }
        }

        // Duración (Solo audio/video)
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(getApplication(), uri)
            val durationString = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            aDuration = durationString?.toLongOrNull() ?: 0L
            retriever.release()
        } catch (e: Exception) {
            aDuration = 0L
        }
        return Pair(aName, aDuration)
    }
}

