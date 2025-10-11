package pe.edu.upc.gigumobile.pulls.data.remote

data class PullDto(
    val id: Int,
    val sellerId: Int,
    val buyerId: Int,
    val gigId: Int,
    val priceInit: Double,
    val priceUpdate: Double,
    val state: String
)

data class CreatePullRequest(
    val sellerId: Int,
    val gigId: Int,
    val priceInit: Double,
    val priceUpdate: Double,
    val buyerId: Int,
    val state: String
)

data class UpdatePullRequest(
    val newPrice: Double,
    val newState: String
)

