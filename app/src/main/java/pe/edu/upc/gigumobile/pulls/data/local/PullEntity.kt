package pe.edu.upc.gigumobile.pulls.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pulls",
    indices = [Index(value = ["gigId", "buyerId"], unique = true)]
)
data class PullEntity(
    @PrimaryKey val id: Int,
    val sellerId: Int,
    val buyerId: Int,
    val gigId: Int,
    val priceInit: Double,
    val priceUpdate: Double,
    val state: String // guardamos como string: "pending", "in_process", etc.
)

