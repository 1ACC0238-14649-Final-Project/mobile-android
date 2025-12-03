package pe.edu.upc.gigumobile.gigs.data.remote

// Based in Swagger. If a certain value does not reach the list, 
// it will arrive as null. The mapper will take care of this.
data class GigDto(
    val id: String,
    val image: String? = null,
    val title: String,
    val description: String? = null,
    val sellerId: String,
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
