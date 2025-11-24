package com.uiii_t8.data.repository

import com.uiii_t8.data.MediaItem
import com.uiii_t8.data.MediaType
import com.uiii_t8.data.dao.MediaDao
import kotlinx.coroutines.flow.Flow

// --- 4. Repositorio (Control de datos guardados con Room) —
class MediaRepository(private val mediaDao: MediaDao) {
    fun getAllAudio(): Flow<List<MediaItem>> {
        return mediaDao.getMediaByType(MediaType.AUDIO)
    }
    fun getAllImages(): Flow<List<MediaItem>> {
        return mediaDao.getMediaByType(MediaType.IMAGE)
    }
    fun getAllVideos(): Flow<List<MediaItem>> {
        return mediaDao.getMediaByType(MediaType.VIDEO)
    }
    suspend fun insertMedia(item: MediaItem) {
        mediaDao.insertMedia(item)
    }
}