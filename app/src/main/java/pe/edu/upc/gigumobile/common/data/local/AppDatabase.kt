package pe.edu.upc.gigumobile.common.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pe.edu.upc.gigumobile.users.data.local.UserDao
import pe.edu.upc.gigumobile.users.data.local.UserEntity
import pe.edu.upc.gigumobile.gigs.data.local.GigDao
import pe.edu.upc.gigumobile.gigs.data.local.GigEntity

@Database(
    entities = [
        UserEntity::class,
        GigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getUserDao(): UserDao
    abstract fun getGigDao(): GigDao

    companion object {
        // De 1 -> 2: creamos la tabla 'gigs'
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `gigs` (
                        `id` TEXT NOT NULL,
                        `image` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `sellerName` TEXT NOT NULL,
                        `price` REAL NOT NULL,
                        `category` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `gigs` ADD COLUMN `tagsJson` TEXT NOT NULL DEFAULT '[]'")
                db.execSQL("ALTER TABLE `gigs` ADD COLUMN `deliveryDays` INTEGER")
                db.execSQL("ALTER TABLE `gigs` ADD COLUMN `extraFeaturesJson` TEXT NOT NULL DEFAULT '[]'")
            }
        }
    }
}

