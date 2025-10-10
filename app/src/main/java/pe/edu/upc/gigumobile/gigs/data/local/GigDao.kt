package pe.edu.upc.gigumobile.gigs.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface GigDao {
    @Query("SELECT * FROM gigs")
    suspend fun getAll(): List<GigEntity>

    @Query("SELECT * FROM gigs WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): GigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<GigEntity>)

    @Query("DELETE FROM gigs")
    suspend fun clearAll()
}
