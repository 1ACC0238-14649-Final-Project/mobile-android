package pe.edu.upc.gigumobile.pulls.data.local

import pe.edu.upc.gigumobile.pulls.domain.model.Pull
import pe.edu.upc.gigumobile.pulls.domain.model.PullState

fun PullEntity.toDomain(): Pull {
    return Pull(
        id = id,
        sellerId = sellerId,
        gigId = gigId,
        priceInit = priceInit,
        priceUpdate = priceUpdate,
        buyerId = buyerId,
        state = PullState.fromString(state)
    )
}

fun Pull.toEntity(): PullEntity {
    return PullEntity(
        id = id,
        sellerId = sellerId,
        gigId = gigId,
        priceInit = priceInit,
        priceUpdate = priceUpdate,
        buyerId = buyerId,
        state = PullState.toString(state)
    )
}

