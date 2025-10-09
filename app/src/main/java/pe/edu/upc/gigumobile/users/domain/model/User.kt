package pe.edu.upc.gigumobile.users.domain.model

data class User(
    val email: String,
    val name: String = "",
    val lastname: String = "",
    val role: String = "",
    val image: String? = null,
    val token: String? = null
)
