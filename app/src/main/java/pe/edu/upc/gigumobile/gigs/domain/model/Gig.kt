package pe.edu.upc.gigumobile.gigs.domain.model

data class Gig(
    val id: String,
    val image: String,
    val title: String,
    val description: String,
    val sellerName: String,
    val price: Double,
    val category: String,
    val tags: List<String> = emptyList(),
    val deliveryDays: Int? = null,
    val extraFeatures: List<String> = emptyList(),
    // NUEVO (solo para red):
    val sellerId: String? = null,
    val sellerAvatar: String? = null
)

