package pe.edu.upc.gigumobile.users.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val name: String = "",
    val lastname: String = "",
    val role: String = "",
    val image: String? = null,
    val token: String? = null
)
