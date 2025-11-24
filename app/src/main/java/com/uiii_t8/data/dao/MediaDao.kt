package com.uiii_t8.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.uiii_t8.data.MediaItem
import com.uiii_t8.data.MediaType
import kotlinx.coroutines.flow.Flow


@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(item: MediaItem)
    @Query("SELECT * FROM media_items WHERE type = :type ORDER BY date DESC")
        fun getMediaByType(type: MediaType): Flow<List<MediaItem>>

}