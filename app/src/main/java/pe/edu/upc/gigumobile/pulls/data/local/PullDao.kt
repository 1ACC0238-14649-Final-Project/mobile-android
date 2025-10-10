package pe.edu.upc.gigumobile.pulls.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PullDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pull: PullEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(pulls: List<PullEntity>)

    @Query("SELECT * FROM pulls")
    suspend fun getAll(): List<PullEntity>

    @Query("SELECT * FROM pulls WHERE id = :id")
    suspend fun getById(id: Int): PullEntity?

    @Query("SELECT * FROM pulls WHERE buyerId = :buyerId")
    suspend fun getByBuyerId(buyerId: Int): List<PullEntity>

    @Query("SELECT * FROM pulls WHERE gigId = :gigId AND buyerId = :buyerId LIMIT 1")
    suspend fun findByGigAndBuyer(gigId: Int, buyerId: Int): PullEntity?

    @Query("DELETE FROM pulls")
    suspend fun clearAll()

    @Query("DELETE FROM pulls WHERE id = :id")
    suspend fun deleteById(id: Int)
}

