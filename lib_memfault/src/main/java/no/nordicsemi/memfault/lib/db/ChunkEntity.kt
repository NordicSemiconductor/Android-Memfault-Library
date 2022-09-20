package no.nordicsemi.memfault.lib.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chunks")
internal data class ChunkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "chunk_number")
    val chunkNumber: Int,
    @ColumnInfo(name = "data", typeAffinity = ColumnInfo.BLOB)
    val data: ByteArray,
    @ColumnInfo(name = "device_id")
    val deviceId: String,
    @ColumnInfo(name = "is_uploaded")
    val isUploaded: Boolean
)
