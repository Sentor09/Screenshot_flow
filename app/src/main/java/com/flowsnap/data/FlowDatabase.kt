package com.flowsnap.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.Transaction
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "flows")
data class FlowEntity(@PrimaryKey val id: String, val name: String, val createdAt: Long, val updatedAt: Long, val diagramVersion: String = "v1.0", val createdBy: String = "You", val showTitle: Boolean = true, val showVersion: Boolean = true, val showCreatedAt: Boolean = true, val showCreatedBy: Boolean = true, val showAppVersion: Boolean = false, val showMobileStencils: Boolean = true, val connectorColor: Long = 0xFF69B83AL)

@Entity(tableName = "screens", foreignKeys = [ForeignKey(entity = FlowEntity::class, parentColumns = ["id"], childColumns = ["flowId"], onDelete = ForeignKey.CASCADE)], indices = [Index("flowId")])
data class ScreenEntity(@PrimaryKey val id: String, val flowId: String, val imagePath: String, val markerX: Float, val markerY: Float, val position: Int, val createdAt: Long)

@Entity(tableName = "transitions", foreignKeys = [
    ForeignKey(entity = FlowEntity::class, parentColumns = ["id"], childColumns = ["flowId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = ScreenEntity::class, parentColumns = ["id"], childColumns = ["fromScreenId"], onDelete = ForeignKey.CASCADE),
    ForeignKey(entity = ScreenEntity::class, parentColumns = ["id"], childColumns = ["toScreenId"], onDelete = ForeignKey.CASCADE)
], indices = [Index("flowId"), Index("fromScreenId"), Index("toScreenId")])
data class TransitionEntity(@PrimaryKey val id: String, val flowId: String, val fromScreenId: String, val toScreenId: String, val fromMarkerX: Float, val fromMarkerY: Float, val createdAt: Long)

@Dao
interface FlowDao {
    @Query("SELECT * FROM flows ORDER BY updatedAt DESC") fun observeAll(): Flow<List<FlowEntity>>
    @Query("SELECT * FROM flows WHERE id = :flowId LIMIT 1") fun observeFlow(flowId: String): Flow<FlowEntity?>
    @Query("SELECT * FROM flows WHERE id = :flowId LIMIT 1") suspend fun flow(flowId: String): FlowEntity?
    @Insert(onConflict = OnConflictStrategy.ABORT) suspend fun insertFlow(flow: FlowEntity)
    @Update suspend fun updateFlow(flow: FlowEntity)
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position") fun observeScreens(flowId: String): Flow<List<ScreenEntity>>
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position") suspend fun screensForFlow(flowId: String): List<ScreenEntity>
    @Query("SELECT imagePath FROM screens") suspend fun referencedImagePaths(): List<String>
    @Insert suspend fun insertScreen(screen: ScreenEntity)
    @Query("SELECT COUNT(*) FROM screens WHERE flowId = :flowId") suspend fun screenCount(flowId: String): Int
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position DESC LIMIT 1") suspend fun lastScreen(flowId: String): ScreenEntity?
    @Insert suspend fun insertTransition(transition: TransitionEntity)
    @Query("SELECT * FROM transitions WHERE flowId = :flowId ORDER BY createdAt") fun observeTransitions(flowId: String): Flow<List<TransitionEntity>>
    @Query("UPDATE flows SET updatedAt = :updatedAt WHERE id = :flowId") suspend fun touchFlow(flowId: String, updatedAt: Long)
    @Query("DELETE FROM flows WHERE id = :flowId") suspend fun deleteFlow(flowId: String)

    @Transaction
    suspend fun persistCapture(flowId: String, imagePath: String, markerX: Float, markerY: Float, now: Long) {
        val previous = lastScreen(flowId)
        val screen = ScreenEntity(UUID.randomUUID().toString(), flowId, imagePath, markerX, markerY, screenCount(flowId), now)
        insertScreen(screen)
        if (previous != null) insertTransition(TransitionEntity(UUID.randomUUID().toString(), flowId, previous.id, screen.id, previous.markerX, previous.markerY, now))
        touchFlow(flowId, now)
    }

    @Transaction
    suspend fun deleteFlowAndGetImagePaths(flowId: String): List<String> {
        val paths = screensForFlow(flowId).map { it.imagePath }
        deleteFlow(flowId)
        return paths
    }
}

@Database(entities = [FlowEntity::class, ScreenEntity::class, TransitionEntity::class], version = 9, exportSchema = true)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun flowDao(): FlowDao
    companion object {
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `screens_new` (`id` TEXT NOT NULL, `flowId` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `markerX` REAL NOT NULL, `markerY` REAL NOT NULL, `position` INTEGER NOT NULL, `canvasX` REAL NOT NULL, `canvasY` REAL NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`flowId`) REFERENCES `flows`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `screens_new` SELECT `id`,`flowId`,`imagePath`,`markerX`,`markerY`,`position`,`canvasX`,`canvasY`,`createdAt` FROM `screens`")
                db.execSQL("DROP TABLE `screens`")
                db.execSQL("ALTER TABLE `screens_new` RENAME TO `screens`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_screens_flowId` ON `screens` (`flowId`)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `transitions_new` (`id` TEXT NOT NULL, `flowId` TEXT NOT NULL, `fromScreenId` TEXT NOT NULL, `toScreenId` TEXT NOT NULL, `fromMarkerX` REAL NOT NULL, `fromMarkerY` REAL NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`flowId`) REFERENCES `flows`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`fromScreenId`) REFERENCES `screens`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`toScreenId`) REFERENCES `screens`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `transitions_new` SELECT `id`,`flowId`,`fromScreenId`,`toScreenId`,`fromMarkerX`,`fromMarkerY`,`createdAt` FROM `transitions`")
                db.execSQL("DROP TABLE `transitions`")
                db.execSQL("ALTER TABLE `transitions_new` RENAME TO `transitions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_flowId` ON `transitions` (`flowId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_fromScreenId` ON `transitions` (`fromScreenId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_toScreenId` ON `transitions` (`toScreenId`)")
            }
        }
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `screens_new` (`id` TEXT NOT NULL, `flowId` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `markerX` REAL NOT NULL, `markerY` REAL NOT NULL, `position` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`flowId`) REFERENCES `flows`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `screens_new` SELECT `id`,`flowId`,`imagePath`,`markerX`,`markerY`,`position`,`createdAt` FROM `screens`")
                // Rebuild children before replacing their foreign-key parent.
                db.execSQL("CREATE TABLE IF NOT EXISTS `transitions_new` (`id` TEXT NOT NULL, `flowId` TEXT NOT NULL, `fromScreenId` TEXT NOT NULL, `toScreenId` TEXT NOT NULL, `fromMarkerX` REAL NOT NULL, `fromMarkerY` REAL NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`flowId`) REFERENCES `flows`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`fromScreenId`) REFERENCES `screens`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`toScreenId`) REFERENCES `screens`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `transitions_new` SELECT `id`,`flowId`,`fromScreenId`,`toScreenId`,`fromMarkerX`,`fromMarkerY`,`createdAt` FROM `transitions`")
                db.execSQL("DROP TABLE `transitions`")
                db.execSQL("DROP TABLE `screens`")
                db.execSQL("ALTER TABLE `screens_new` RENAME TO `screens`")
                db.execSQL("ALTER TABLE `transitions_new` RENAME TO `transitions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_screens_flowId` ON `screens` (`flowId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_flowId` ON `transitions` (`flowId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_fromScreenId` ON `transitions` (`fromScreenId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_toScreenId` ON `transitions` (`toScreenId`)")
            }
        }
        fun create(context: Context) = Room.databaseBuilder(context.applicationContext, FlowDatabase::class.java, "flowsnap.db").addMigrations(MIGRATION_7_8, MIGRATION_8_9).build()
    }
}
