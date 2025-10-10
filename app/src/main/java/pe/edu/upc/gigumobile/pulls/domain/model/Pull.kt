package pe.edu.upc.gigumobile.pulls.domain.model

data class Pull(
    val id: Int = 0,
    val sellerId: Int,
    val buyerId: Int,
    val gigId: Int,
    val priceInit: Double,
    val priceUpdate: Double,
    val state: PullState
)

enum class PullState {
    PENDING,      // "pending"
    IN_PROCESS,   // "in_process"
    PAYED,        // "payed"
    COMPLETE;     // "complete"

    companion object {
        fun fromString(value: String): PullState {
            return when (value.lowercase()) {
                "pending" -> PENDING
                "in_process" -> IN_PROCESS
                "payed" -> PAYED
                "complete" -> COMPLETE
                else -> PENDING
            }
        }

        fun toString(state: PullState): String {
            return when (state) {
                PENDING -> "pending"
                IN_PROCESS -> "in_process"
                PAYED -> "payed"
                COMPLETE -> "complete"
            }
        }
    }
}

