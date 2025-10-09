package pe.edu.upc.gigumobile.users.data.remote

data class SignUpRequest(
    val name: String,
    val lastname: String,
    val email: String,
    val password: String,
    val role: String,
    val image: String
)
