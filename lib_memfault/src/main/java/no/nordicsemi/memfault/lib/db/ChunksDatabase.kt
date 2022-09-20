package no.nordicsemi.memfault.lib.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ChunkEntity::class], version = 1)
internal abstract class ChunksDatabase : RoomDatabase() {

    abstract fun chunksDao(): ChunksDao
}
