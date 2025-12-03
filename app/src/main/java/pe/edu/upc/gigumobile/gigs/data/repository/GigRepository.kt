package pe.edu.upc.gigumobile.gigs.data.repository

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pe.edu.upc.gigumobile.common.Constants
import pe.edu.upc.gigumobile.common.Resource
import pe.edu.upc.gigumobile.gigs.data.local.GigDao
import pe.edu.upc.gigumobile.gigs.data.local.toDomain
import pe.edu.upc.gigumobile.gigs.data.local.toEntity
import pe.edu.upc.gigumobile.gigs.data.remote.GigDto
import pe.edu.upc.gigumobile.gigs.data.remote.GigService
import pe.edu.upc.gigumobile.gigs.domain.model.Gig
import pe.edu.upc.gigumobile.users.data.local.UserDao

class GigRepository(
    private val service: GigService,
    private val gigDao: GigDao,
    private val userDao: UserDao,
    // If later you receive an endpoint to fetch the seller (name/avatar), inject it here:
    private val userService: Any? = null // Placeholder to avoid breaking the method signature
) {
    private val gson = Gson()

    private suspend fun authHeader(): String? = withContext(Dispatchers.IO) {
        userDao.fetchAny()?.token?.let { "Bearer $it" }
    }

    /**
     * List of gigs (paginated). The API returns an OBJECT with the key "data" -> array.
     */
    suspend fun getAllGigs(
        page: Int = 1,
        pageSize: Int = 10,
        searchTerm: String? = null,
        sortBy: String = "createdAt",
        descending: Boolean = true,
        minPrice: Double? = null,
        maxPrice: Double? = null,
        maxDeliveryDays: Int? = null
    ): Resource<List<Gig>> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            val resp = service.getGigsRaw(
                auth = auth,
                page = page,
                pageSize = pageSize,
                searchTerm = searchTerm,
                minPrice = minPrice,
                maxPrice = maxPrice,
                maxDeliveryDays = maxDeliveryDays,
                sortBy = sortBy,
                descending = descending
            )

            if (!resp.isSuccessful) {
                val local = gigDao.getAll().map { it.toDomain() }
                return@withContext if (local.isNotEmpty()) Resource.Success(local)
                else Resource.Error("Error ${resp.code()}")
            }

            val bodyStr = resp.body()?.string().orEmpty()

            // Extracts the array: we now know it is under "data".
            val itemsArray: JSONArray = when {
                bodyStr.trim().startsWith("[") -> JSONArray(bodyStr)
                else -> {
                    val obj = JSONObject(bodyStr)
                    when {
                        obj.has("data") -> obj.getJSONArray("data")
                        obj.has("items") -> obj.getJSONArray("items")
                        obj.has("results") -> obj.getJSONArray("results")
                        else -> JSONArray()
                    }
                }
            }

            val listType = object : TypeToken<List<GigDto>>() {}.type
            val dtoList: List<GigDto> = gson.fromJson(itemsArray.toString(), listType)

            val gigs = dtoList.map { d ->
                Gig(
                    id = d.id,
                    image = d.image ?: "",
                    title = d.title,
                    description = d.description ?: "",
                    // For now we don’t have the seller’s name/photo in the Gig API:
                    sellerName = "",
                    price = d.price,
                    category = d.category,
                    tags = d.tags ?: emptyList(),
                    deliveryDays = d.deliveryDays,
                    extraFeatures = d.extraFeatures ?: emptyList(),
                    sellerId = d.sellerId?.toString(), // Retain the sellerId for potential future use
                    gigLink = "${Constants.BASE_URL}gig/${d.id}"
                )
            }

            // Cache in Room
            gigDao.clearAll()
            gigDao.insertAll(gigs.map { it.toEntity() })

            Resource.Success(gigs)
        } catch (e: Exception) {
            val local = gigDao.getAll().map { it.toDomain() }
            if (local.isNotEmpty()) Resource.Success(local)
            else Resource.Error(e.message ?: "Error al obtener gigs")
        }
    }

    /**
     * Detail for id.
     */
    suspend fun getGigById(id: String): Resource<Gig> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            val resp = service.getGigDetail(id, auth)
            if (resp.isSuccessful) {
                val d = resp.body()!!
                val gig = Gig(
                    id = d.id,
                    image = d.image ?: "",
                    title = d.title,
                    description = d.description ?: "",
                    sellerName = "", // Same as before: no endpoint
                    price = d.price,
                    category = d.category,
                    tags = d.tags ?: emptyList(),
                    deliveryDays = d.deliveryDays,
                    extraFeatures = d.extraFeatures ?: emptyList(),
                    sellerId = d.sellerId?.toString()
                )
                Resource.Success(gig)
            } else {
                // Fallback to cache
                val local = gigDao.getById(id)?.toDomain()
                if (local != null) Resource.Success(local)
                else Resource.Error("Error ${resp.code()}")
            }
        } catch (e: Exception) {
            val local = gigDao.getById(id)?.toDomain()
            if (local != null) Resource.Success(local)
            else Resource.Error(e.message ?: "Error al obtener detalle")
        }
    }
}
