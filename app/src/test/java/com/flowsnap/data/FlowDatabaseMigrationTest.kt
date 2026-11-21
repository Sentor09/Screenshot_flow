package com.flowsnap.data

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
class FlowDatabaseMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "migration-${UUID.randomUUID()}.db"

    @After fun deleteDatabase() {
        context.deleteDatabase(databaseName)
    }

    @Test fun migrationAddsCanvasDefaultsAndLaysOutExistingScreens() {
        val database = createVersion12Database()
        database.execSQL("INSERT INTO `flows` (`id`, `name`, `createdAt`, `updatedAt`, `diagramVersion`, `createdBy`, `showTitle`, `showVersion`, `showCreatedAt`, `showCreatedBy`, `showAppVersion`, `showMobileStencils`, `connectorColor`, `preferredRows`, `preferredColumns`) VALUES ('flow-a', 'A', 1, 1, 'v1', 'You', 1, 1, 1, 1, 0, 1, 1, 2, 5)")
        database.execSQL("INSERT INTO `flows` (`id`, `name`, `createdAt`, `updatedAt`, `diagramVersion`, `createdBy`, `showTitle`, `showVersion`, `showCreatedAt`, `showCreatedBy`, `showAppVersion`, `showMobileStencils`, `connectorColor`, `preferredRows`, `preferredColumns`) VALUES ('flow-b', 'B', 1, 1, 'v1', 'You', 1, 1, 1, 1, 0, 1, 1, 2, 5)")
        insertScreen(database, "a0", "flow-a", 0)
        insertScreen(database, "a4", "flow-a", 4)
        insertScreen(database, "a5", "flow-a", 5)
        insertScreen(database, "b0", "flow-b", 0)

        FlowDatabase.MIGRATION_12_13.migrate(database)

        database.query("SELECT `canvasZoom`, `canvasPanX`, `canvasPanY` FROM `flows` WHERE `id` = 'flow-a'").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1f, cursor.getFloat(0), 0f)
            assertEquals(0f, cursor.getFloat(1), 0f)
            assertEquals(0f, cursor.getFloat(2), 0f)
        }
        assertScreenPosition(database, "a0", 0f, 0f)
        assertScreenPosition(database, "a4", 1_600f, 0f)
        assertScreenPosition(database, "a5", 0f, 760f)
        assertScreenPosition(database, "b0", 0f, 0f)
        database.close()
    }

    private fun createVersion12Database(): SupportSQLiteDatabase {
        val callback = object : SupportSQLiteOpenHelper.Callback(12) {
            override fun onCreate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE `flows` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, `diagramVersion` TEXT NOT NULL, `createdBy` TEXT NOT NULL, `showTitle` INTEGER NOT NULL, `showVersion` INTEGER NOT NULL, `showCreatedAt` INTEGER NOT NULL, `showCreatedBy` INTEGER NOT NULL, `showAppVersion` INTEGER NOT NULL, `showMobileStencils` INTEGER NOT NULL, `connectorColor` INTEGER NOT NULL, `preferredRows` INTEGER NOT NULL, `preferredColumns` INTEGER NOT NULL, PRIMARY KEY(`id`))")
                db.execSQL("CREATE TABLE `screens` (`id` TEXT NOT NULL, `flowId` TEXT NOT NULL, `imagePath` TEXT NOT NULL, `markerX` REAL NOT NULL, `markerY` REAL NOT NULL, `position` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
            }

            override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
        }
        return FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context).name(databaseName).callback(callback).build()
        ).writableDatabase
    }

    private fun insertScreen(database: SupportSQLiteDatabase, id: String, flowId: String, position: Int) {
        database.execSQL("INSERT INTO `screens` (`id`, `flowId`, `imagePath`, `markerX`, `markerY`, `position`, `createdAt`) VALUES ('$id', '$flowId', 'image', 0.5, 0.5, $position, 1)")
    }

    private fun assertScreenPosition(database: SupportSQLiteDatabase, id: String, expectedX: Float, expectedY: Float) {
        database.query("SELECT `canvasX`, `canvasY` FROM `screens` WHERE `id` = '$id'").use { cursor ->
            cursor.moveToFirst()
            assertEquals(expectedX, cursor.getFloat(0), 0f)
            assertEquals(expectedY, cursor.getFloat(1), 0f)
        }
    }
}
