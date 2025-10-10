package pe.edu.upc.gigumobile.common.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import pe.edu.upc.gigumobile.users.data.local.UserDao
import pe.edu.upc.gigumobile.users.data.local.UserEntity
import pe.edu.upc.gigumobile.gigs.data.local.GigDao
import pe.edu.upc.gigumobile.gigs.data.local.GigEntity
import pe.edu.upc.gigumobile.pulls.data.local.PullDao
import pe.edu.upc.gigumobile.pulls.data.local.PullEntity

@Database(
    entities = [
        UserEntity::class,
        GigEntity::class,
        PullEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getUserDao(): UserDao
    abstract fun getGigDao(): GigDao
    abstract fun getPullDao(): PullDao

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

        // De 3 -> 4: creamos la tabla 'pulls' (versión antigua - no usar)
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pulls` (
                        `id` TEXT NOT NULL,
                        `sellerId` INTEGER NOT NULL,
                        `gigId` INTEGER NOT NULL,
                        `priceInit` REAL NOT NULL,
                        `priceUpdate` REAL NOT NULL,
                        `buyerId` INTEGER NOT NULL,
                        `state` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        // De 4 -> 5: recreamos la tabla 'pulls' con id INTEGER
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Eliminar la tabla anterior
                db.execSQL("DROP TABLE IF EXISTS `pulls`")
                
                // Crear la nueva tabla con id INTEGER
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pulls` (
                        `id` INTEGER NOT NULL,
                        `sellerId` INTEGER NOT NULL,
                        `buyerId` INTEGER NOT NULL,
                        `gigId` INTEGER NOT NULL,
                        `priceInit` REAL NOT NULL,
                        `priceUpdate` REAL NOT NULL,
                        `state` TEXT NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        // De 5 -> 6: agregar constraint UNIQUE para evitar pulls duplicados
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Crear nueva tabla con constraint UNIQUE
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `pulls_new` (
                        `id` INTEGER NOT NULL,
                        `sellerId` INTEGER NOT NULL,
                        `buyerId` INTEGER NOT NULL,
                        `gigId` INTEGER NOT NULL,
                        `priceInit` REAL NOT NULL,
                        `priceUpdate` REAL NOT NULL,
                        `state` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        UNIQUE(`gigId`, `buyerId`)
                    )
                    """.trimIndent()
                )
                
                // Copiar datos existentes (si hay)
                db.execSQL(
                    """
                    INSERT OR IGNORE INTO `pulls_new` 
                    SELECT * FROM `pulls`
                    """.trimIndent()
                )
                
                // Eliminar tabla antigua
                db.execSQL("DROP TABLE `pulls`")
                
                // Renombrar nueva tabla
                db.execSQL("ALTER TABLE `pulls_new` RENAME TO `pulls`")
            }
        }
    }
}

