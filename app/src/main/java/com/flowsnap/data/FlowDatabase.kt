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
import androidx.room.Transaction
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Entity(tableName = "flows")
data class FlowEntity(@PrimaryKey val id: String, val name: String, val createdAt: Long, val updatedAt: Long, val diagramVersion: String = "v1.0", val createdBy: String = "You", val showTitle: Boolean = true, val showVersion: Boolean = true, val showCreatedAt: Boolean = true, val showCreatedBy: Boolean = true, val showAppVersion: Boolean = false, val showMobileStencils: Boolean = true, val connectorColor: Long = 0xFF69B83AL, val preferredRows: Int = 2, val preferredColumns: Int = 5)

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
    @Query("SELECT EXISTS(SELECT 1 FROM flows WHERE id = :flowId)") suspend fun flowExists(flowId: String): Boolean
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(flow: FlowEntity)
    @Insert suspend fun insertFlow(flow: FlowEntity)
    @Update suspend fun updateFlow(flow: FlowEntity)
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position") fun observeScreens(flowId: String): Flow<List<ScreenEntity>>
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position") suspend fun screensForFlow(flowId: String): List<ScreenEntity>
    @Query("SELECT imagePath FROM screens") suspend fun referencedImagePaths(): List<String>
    @Insert suspend fun insertScreen(screen: ScreenEntity)
    @Update suspend fun updateScreen(screen: ScreenEntity)
    @Query("UPDATE screens SET markerX = :markerX, markerY = :markerY WHERE id = :screenId")
    suspend fun updateScreenMarker(screenId: String, markerX: Float, markerY: Float)
    @Query("UPDATE transitions SET fromMarkerX = :markerX, fromMarkerY = :markerY WHERE fromScreenId = :screenId")
    suspend fun updateTransitionMarkersForScreen(screenId: String, markerX: Float, markerY: Float)
    @Query("SELECT COUNT(*) FROM screens WHERE flowId = :flowId") suspend fun screenCount(flowId: String): Int
    @Query("SELECT * FROM screens WHERE flowId = :flowId ORDER BY position DESC LIMIT 1") suspend fun lastScreen(flowId: String): ScreenEntity?
    @Insert suspend fun insertTransition(transition: TransitionEntity)
    @Query("SELECT * FROM transitions WHERE flowId = :flowId ORDER BY createdAt") fun observeTransitions(flowId: String): Flow<List<TransitionEntity>>
    @Query("UPDATE flows SET updatedAt = :updatedAt WHERE id = :flowId") suspend fun touchFlow(flowId: String, updatedAt: Long)
    @Query("DELETE FROM flows WHERE id = :flowId") suspend fun deleteFlow(flowId: String)
    @Query("SELECT imagePath FROM screens WHERE id = :screenId LIMIT 1") suspend fun screenImagePath(screenId: String): String?
    @Query("DELETE FROM screens WHERE id = :screenId") suspend fun deleteScreen(screenId: String)

    @Transaction
    suspend fun deleteScreenAndGetImagePath(screenId: String): String? {
        val imagePath = screenImagePath(screenId)
        deleteScreen(screenId)
        return imagePath
    }

    @Transaction
    suspend fun updateScreenPointer(screen: ScreenEntity, markerX: Float, markerY: Float) {
        updateScreenMarker(screen.id, markerX, markerY)
        // Connectors render their start point from TransitionEntity, so keep
        // every outgoing connector synchronized with the edited marker.
        updateTransitionMarkersForScreen(screen.id, markerX, markerY)
        touchFlow(screen.flowId, System.currentTimeMillis())
    }

    @Transaction
    suspend fun persistCapture(flowId: String, imagePath: String, markerX: Float, markerY: Float, now: Long): Boolean {
        // The diagram can be deleted while a capture review is open. Checking
        // inside this transaction prevents a foreign-key crash and keeps the
        // unowned capture from appearing in a different flow.
        if (!flowExists(flowId)) return false
        val previous = lastScreen(flowId)
        val screen = ScreenEntity(UUID.randomUUID().toString(), flowId, imagePath, markerX, markerY, screenCount(flowId), now)
        insertScreen(screen)
        if (previous != null) insertTransition(TransitionEntity(UUID.randomUUID().toString(), flowId, previous.id, screen.id, previous.markerX, previous.markerY, now))
        touchFlow(flowId, now)
        return true
    }

    @Transaction
    suspend fun deleteFlowAndGetImagePaths(flowId: String): List<String> {
        val paths = screensForFlow(flowId).map { it.imagePath }
        deleteFlow(flowId)
        return paths
    }
}

@Database(entities = [FlowEntity::class, ScreenEntity::class, TransitionEntity::class], version = 11, exportSchema = true)
abstract class FlowDatabase : RoomDatabase() {
    abstract fun flowDao(): FlowDao
    companion object {
        // Version 4 was shipped before diagram labels, appearance settings, and
        // database foreign keys were introduced. Rebuild the three tables so
        // existing diagrams and their screenshots survive the upgrade.
        private val MIGRATION_4_9 = object : Migration(4, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `flows_new` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `diagramVersion` TEXT NOT NULL DEFAULT 'v1.0', `createdBy` TEXT NOT NULL DEFAULT 'You', `showTitle` INTEGER NOT NULL DEFAULT 1, `showVersion` INTEGER NOT NULL DEFAULT 1, `showCreatedAt` INTEGER NOT NULL DEFAULT 1, `showCreatedBy` INTEGER NOT NULL DEFAULT 1, `showAppVersion` INTEGER NOT NULL DEFAULT 0, `showMobileStencils` INTEGER NOT NULL DEFAULT 1, `connectorColor` INTEGER NOT NULL DEFAULT 4285110330, PRIMARY KEY(`id`))")
                db.execSQL("INSERT INTO `flows_new` (`id`,`name`,`createdAt`,`updatedAt`) SELECT `id`,`name`,`createdAt`,`updatedAt` FROM `flows`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `screens_new` (`id` TEXT NOT NULL, `flowId` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `markerX` REAL NOT NULL, `markerY` REAL NOT NULL, `position` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`flowId`) REFERENCES `flows`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `screens_new` (`id`,`flowId`,`imagePath`,`markerX`,`markerY`,`position`,`createdAt`) SELECT `id`,`flowId`,`imagePath`,`markerX`,`markerY`,`position`,`createdAt` FROM `screens`")
                db.execSQL("CREATE TABLE IF NOT EXISTS `transitions_new` (`id` TEXT NOT NULL, `flowId` TEXT NOT NULL, `fromScreenId` TEXT NOT NULL, `toScreenId` TEXT NOT NULL, `fromMarkerX` REAL NOT NULL, `fromMarkerY` REAL NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`flowId`) REFERENCES `flows`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`fromScreenId`) REFERENCES `screens`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE, FOREIGN KEY(`toScreenId`) REFERENCES `screens`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("INSERT INTO `transitions_new` (`id`,`flowId`,`fromScreenId`,`toScreenId`,`fromMarkerX`,`fromMarkerY`,`createdAt`) SELECT `id`,`flowId`,`fromScreenId`,`toScreenId`,`fromMarkerX`,`fromMarkerY`,`createdAt` FROM `transitions`")
                db.execSQL("DROP TABLE `transitions`")
                db.execSQL("DROP TABLE `screens`")
                db.execSQL("DROP TABLE `flows`")
                db.execSQL("ALTER TABLE `flows_new` RENAME TO `flows`")
                db.execSQL("ALTER TABLE `screens_new` RENAME TO `screens`")
                db.execSQL("ALTER TABLE `transitions_new` RENAME TO `transitions`")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_screens_flowId` ON `screens` (`flowId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_flowId` ON `transitions` (`flowId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_fromScreenId` ON `transitions` (`fromScreenId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transitions_toScreenId` ON `transitions` (`toScreenId`)")
            }
        }
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
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `flows` ADD COLUMN `preferredRows` INTEGER NOT NULL DEFAULT 2")
            }
        }
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `flows` ADD COLUMN `preferredColumns` INTEGER NOT NULL DEFAULT 5")
            }
        }
        fun create(context: Context) = Room.databaseBuilder(context.applicationContext, FlowDatabase::class.java, "flowsnap.db")
            .addMigrations(MIGRATION_4_9, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
            .build()
    }
}
