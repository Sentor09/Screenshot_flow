package com.flowsnap.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "flows")
data class FlowEntity(@PrimaryKey val id: String, val name: String, val createdAt: Long, val updatedAt: Long)

@Entity(tableName = "screens")
data class ScreenEntity(
    @PrimaryKey val id: String,
    val flowId: String,
    val imagePath: String,
    val markerX: Float,
    val markerY: Float,
    val position: Int,
    val canvasX: Float,
    val canvasY: Float,
    val createdAt: Long
)

@Entity(tableName = "transitions")
data class TransitionEntity(
    @PrimaryKey val id: String,
    val flowId: String,
    val fromScreenId: String,
    val toScreenId: String,
    val fromMarkerX: Float,
    val fromMarkerY: Float,
    val createdAt: Long
)

@Dao interface FlowDao {
    @Query("SELECT * FROM flows ORDER BY updatedAt DESC") fun observeAll(): Flow<List<FlowEntity>>
    @Query("SELECT * FROM flows WHERE id = :flowId LIMIT 1") suspend fun flow(flowId: String): FlowEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(flow: FlowEntity)
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position") fun observeScreens(flowId: String): Flow<List<ScreenEntity>>
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position") suspend fun screensForFlow(flowId: String): List<ScreenEntity>
    @Insert suspend fun insertScreen(screen: ScreenEntity)
    @Query("SELECT COUNT(*) FROM screens WHERE flowId = :flowId") suspend fun screenCount(flowId: String): Int
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position DESC LIMIT 1") suspend fun lastScreen(flowId: String): ScreenEntity?
    @Query("DELETE FROM screens WHERE id = :screenId") suspend fun deleteScreen(screenId: String)
    @Insert suspend fun insertTransition(transition: TransitionEntity)
    @Query("SELECT * FROM transitions WHERE flowId = :flowId ORDER BY createdAt") fun observeTransitions(flowId: String): Flow<List<TransitionEntity>>
    @Query("DELETE FROM transitions WHERE flowId = :flowId") suspend fun deleteTransitionsForFlow(flowId: String)
    @Query("DELETE FROM screens WHERE flowId = :flowId") suspend fun deleteScreensForFlow(flowId: String)
    @Query("DELETE FROM flows WHERE id = :flowId") suspend fun deleteFlow(flowId: String)
}

@Database(entities = [FlowEntity::class, ScreenEntity::class, TransitionEntity::class], version = 4, exportSchema = false)
abstract class FlowDatabase : RoomDatabase() { abstract fun flowDao(): FlowDao
    companion object { fun create(context: Context) = Room.databaseBuilder(context, FlowDatabase::class.java, "flowsnap.db").fallbackToDestructiveMigration().build() }
}
