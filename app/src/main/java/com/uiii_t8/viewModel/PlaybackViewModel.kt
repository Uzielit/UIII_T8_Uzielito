package com.uiii_t8.viewModel

//Controla el reproductor de música/video (ExoPlayer) y los sensores (acelerómetro)
import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.uiii_t8.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaybackViewModel(
    application: Application,
    private val settingsRepository: SettingsRepository
) : AndroidViewModel(application), SensorEventListener {

    val exoPlayer: ExoPlayer = ExoPlayer.Builder(application).build()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    // Sensores
    private val sensorManager = getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val _isAccelerometerEnabled = MutableStateFlow(false)
    val isAccelerometerEnabled: StateFlow<Boolean> = _isAccelerometerEnabled.asStateFlow()

    // Volumen (DataStore)
    val currentVolume: StateFlow<Float> = settingsRepository.userVolume
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsRepository.DEFAULT_VOLUME)

    init {
        // Sincronizar volumen inicial
        viewModelScope.launch {
            currentVolume.collect { vol -> exoPlayer.volume = vol }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }
        })
    }

    fun playMedia(uri: String) {
        val mediaItem = MediaItem.fromUri(uri)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
    }

    fun releasePlayer() {
        exoPlayer.release()
        unregisterSensorListener()
    }

    // --- Lógica del Sensor ---
    fun toggleAccelerometer() {
        _isAccelerometerEnabled.update { isEnabled ->
            if (!isEnabled) registerSensorListener() else unregisterSensorListener()
            !isEnabled
        }
    }

    private fun registerSensorListener() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun unregisterSensorListener() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val yValue = event.values[1] // Inclinación lateral
            // Mapeo simple: inclinar izquierda baja volumen, derecha sube
            val newVolume = (yValue + 5f) / 10f
            setVolume(newVolume.coerceIn(0f, 1f))
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun setVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        exoPlayer.volume = clamped
        viewModelScope.launch { settingsRepository.saveVolume(clamped) }
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }
}

