package pe.edu.upc.gigumobile.common.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import pe.edu.upc.gigumobile.users.data.local.UserDao
import pe.edu.upc.gigumobile.users.data.local.UserEntity

@Database(entities = [ UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getUserDao(): UserDao
}
