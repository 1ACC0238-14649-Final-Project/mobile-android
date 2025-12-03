package pe.edu.upc.gigumobile.users.data.remote

data class GoogleLoginRequest(
    val idToken: String,
    val email: String? = null,
    val name: String? = null,
    val image: String? = null
)

