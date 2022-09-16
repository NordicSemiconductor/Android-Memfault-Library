package com.nordicsemi.memfault.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ChunksDao {

    @Query("SELECT * FROM chunks WHERE device_id = :deviceId ORDER BY id DESC")
    fun getAll(deviceId: String): Flow<List<ChunkEntity>>

    @Query("SELECT * FROM chunks WHERE is_uploaded = 0 AND device_id = :deviceId ORDER BY id ASC LIMIT :limit")
    fun getNotUploaded(limit: Int, deviceId: String): List<ChunkEntity>

    @Query("""UPDATE chunks SET is_uploaded = 1 WHERE is_uploaded IN (SELECT is_uploaded FROM chunks WHERE is_uploaded = 0 AND device_id = :deviceId ORDER BY id ASC LIMIT :limit)""")
    fun drop(limit: Int, deviceId: String)

    @Insert
    fun insert(chunk: ChunkEntity)
}
