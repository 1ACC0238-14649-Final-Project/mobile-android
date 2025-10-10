package pe.edu.upc.gigumobile.gigs.data.local

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pe.edu.upc.gigumobile.gigs.domain.model.Gig

private val gson = Gson()
private val listStringType = object : TypeToken<List<String>>() {}.type

fun GigEntity.toDomain(): Gig =
    Gig(
        id = id,
        image = image,
        title = title,
        description = description,
        sellerName = sellerName,
        price = price,
        category = category,
        tags = gson.fromJson(tagsJson, listStringType) ?: emptyList(),
        deliveryDays = deliveryDays,
        extraFeatures = gson.fromJson(extraFeaturesJson, listStringType) ?: emptyList()
    )

fun Gig.toEntity(): GigEntity =
    GigEntity(
        id = id,
        image = image,
        title = title,
        description = description,
        sellerName = sellerName,
        price = price,
        category = category,
        tagsJson = gson.toJson(tags),
        deliveryDays = deliveryDays,
        extraFeaturesJson = gson.toJson(extraFeatures)
    )
