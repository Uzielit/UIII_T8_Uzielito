package com.uiii_t8.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uiii_t8.data.AppDatabase
import com.uiii_t8.data.repository.MediaRepository

class MediaViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica si la clase que se pide es MediaViewModel
        if (modelClass.isAssignableFrom(MediaViewModel::class.java)) {
            // 1. Obtiene la Base de Datos
            val database = AppDatabase.getDatabase(application)
            // 2. Crea el Repositorio usando el DAO
            val repository = MediaRepository(database.mediaDao())

            @Suppress("UNCHECKED_CAST")
            // 3. Devuelve el ViewModel con el repositorio dentro
            return MediaViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
