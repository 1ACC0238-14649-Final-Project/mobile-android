package pe.edu.upc.gigumobile.gigs.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "gigs")
data class GigEntity(
    @PrimaryKey val id: String,
    val image: String,
    val title: String,
    val description: String,
    val sellerName: String,
    val price: Double,
    val category: String,
    val tagsJson: String,
    val deliveryDays: Int?,      // puede ser null
    val extraFeaturesJson: String // JSON de List<String>
)
