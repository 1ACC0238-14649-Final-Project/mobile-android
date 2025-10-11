package pe.edu.upc.gigumobile.pull.presentation

import java.io.Serializable

data class PullUi(
    val title: String,
    val description: String,
    val imageUrl: String?,
    val initialPriceLabel: String,
    val currentPriceLabel: String
) : Serializable