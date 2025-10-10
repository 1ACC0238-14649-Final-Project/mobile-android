package pe.edu.upc.gigumobile.gigs.data.remote

// Basado en tu Swagger. Si algún campo no llega en la lista, vendrá null y lo manejamos en el mapper.
data class GigDto(
    val id: String,
    val image: String? = null,
    val title: String,
    val description: String? = null,
    val sellerId: String,                 // el backend expone sellerId; si luego tienes nombre, mapeamos allá
    val price: Double,
    val tags: List<String>? = null,
    val category: String,
    val deliveryDays: Int? = null,
    val isResponsive: Boolean? = null,
    val revisionCount: Int? = null,
    val pageCount: Int? = null,
    val extraFeatures: List<String>? = null,
    val customAnimations: Boolean? = null
)
