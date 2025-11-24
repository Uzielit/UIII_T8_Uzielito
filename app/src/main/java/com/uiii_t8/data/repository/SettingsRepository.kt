package com.uiii_t8.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException


// El nombre settings es como se llamará el archivo interno en el celular
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {

    private val dataStore = context.dataStore

    companion object {
        val VOLUME_KEY = floatPreferencesKey("volume_level")
        const val DEFAULT_VOLUME = 0.5f
    }

    // 2. Leer el volumen (Flow permite ver cambios en tiempo real)
    val userVolume: Flow<Float> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[VOLUME_KEY] ?: DEFAULT_VOLUME
        }

    // 3. Guardar el volumen
    suspend fun saveVolume(volume: Float) {
        dataStore.edit { settings ->
            val clampedVolume = volume.coerceIn(0.0f, 1.0f)
            settings[VOLUME_KEY] = clampedVolume
        }
    }
}
