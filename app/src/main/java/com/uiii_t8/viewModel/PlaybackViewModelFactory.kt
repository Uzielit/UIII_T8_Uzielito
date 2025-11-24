package com.uiii_t8.viewModel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.uiii_t8.data.repository.SettingsRepository

class PlaybackViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Verifica si la clase que se pide es PlaybackViewModel
        if (modelClass.isAssignableFrom(PlaybackViewModel::class.java)) {
            // 1. Crea el repositorio de configuración (Volumen)
            val repository = SettingsRepository(application)

            @Suppress("UNCHECKED_CAST")
            // 2. Devuelve el ViewModel con el repositorio dentro
            return PlaybackViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}