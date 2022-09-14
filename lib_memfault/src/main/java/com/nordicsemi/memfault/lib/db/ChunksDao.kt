package com.nordicsemi.memfault.lib.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
internal interface ChunksDao {

    @Query("SELECT * FROM chunks")
    fun getAll(): Flow<List<ChunkEntity>>

    @Query("SELECT * FROM chunks WHERE is_uploaded = 0 ORDER BY id DESC LIMIT :limit")
    fun getNotUploaded(limit: Int): List<ChunkEntity>

    @Query("""DELETE FROM chunks WHERE is_uploaded in (SELECT is_uploaded FROM chunks WHERE is_uploaded = 0 ORDER BY id DESC LIMIT :limit)""")
    fun drop(limit: Int)

    @Insert
    fun insert(chunk: ChunkEntity)
}
