package pe.edu.upc.gigumobile.users.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun fetchAny(): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun findByEmail(email: String): UserEntity?

    @Query("DELETE FROM users")
    suspend fun clearAll()
}
