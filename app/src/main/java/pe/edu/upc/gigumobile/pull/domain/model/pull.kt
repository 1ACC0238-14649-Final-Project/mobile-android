package pe.edu.upc.gigumobile.pull.domain.model

import java.util.UUID

typealias EpochSeconds = Long
private fun nowEpochSeconds(): EpochSeconds = System.currentTimeMillis() / 1000

enum class PullStatus { PENDING, ACCEPTED, CANCELED }
enum class Sender { BUYER, SELLER }

data class Money(val amount: Long, val currency: String = "USD") {
    fun formatted(): String = "%s %.2f".format(currency, amount / 100.0)
}

data class Pull(
    val id: String = UUID.randomUUID().toString(),
    val gigId: String,
    val buyerId: String,
    val sellerId: String,
    val initialPrice: Money,
    val currentPrice: Money,
    val description: String,
    val status: PullStatus = PullStatus.PENDING,
    val createdAt: EpochSeconds = nowEpochSeconds(),
    val updatedAt: EpochSeconds = nowEpochSeconds()
)

data class PullMessage(
    val id: String = UUID.randomUUID().toString(),
    val pullId: String,
    val sender: Sender,
    val text: String,
    val timestamp: EpochSeconds = nowEpochSeconds()
)
