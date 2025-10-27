package pe.edu.upc.gigumobile.pulls.data.repository

import android.util.Base64
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pe.edu.upc.gigumobile.common.Resource
import pe.edu.upc.gigumobile.pulls.data.local.PullDao
import pe.edu.upc.gigumobile.pulls.data.local.toDomain
import pe.edu.upc.gigumobile.pulls.data.local.toEntity
import pe.edu.upc.gigumobile.pulls.data.remote.CreatePullRequest
import pe.edu.upc.gigumobile.pulls.data.remote.PullDto
import pe.edu.upc.gigumobile.pulls.data.remote.PullService
import pe.edu.upc.gigumobile.pulls.data.remote.UpdatePullRequest
import pe.edu.upc.gigumobile.pulls.domain.model.Pull
import pe.edu.upc.gigumobile.pulls.domain.model.PullState
import pe.edu.upc.gigumobile.users.data.local.UserDao

class PullRepository(
    private val service: PullService,
    private val pullDao: PullDao,
    private val userDao: UserDao
) {
    private val gson = Gson()

    private suspend fun authHeader(): String? = withContext(Dispatchers.IO) {
        userDao.fetchAny()?.token?.let { "Bearer $it" }
    }

    suspend fun getCurrentUserId(): Int {
        return withContext(Dispatchers.IO) {
            try {
                val token = userDao.fetchAny()?.token ?: return@withContext 1
                
                // Decodificar el JWT token para extraer el userId
                val parts = token.split(".")
                if (parts.size != 3) return@withContext 1
                
                // Decodificar el payload (segunda parte del JWT)
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val jsonPayload = JSONObject(payload)
                
                // Extraer el sid (user ID) del claim
                val sidClaim = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/sid"
                if (jsonPayload.has(sidClaim)) {
                    jsonPayload.getString(sidClaim).toIntOrNull() ?: 1
                } else {
                    1
                }
            } catch (e: Exception) {
                // Si hay error al decodificar, retornar 1 como fallback
                1
            }
        }
    }

    /**
     * Crear un nuevo pull
     */
    suspend fun createPull(
        sellerId: Int,
        gigId: Int,
        priceInit: Double,
        priceUpdate: Double,
        buyerId: Int,
        state: PullState = PullState.PENDING
    ): Resource<Pull> = withContext(Dispatchers.IO) {
        try {
            // Primero sincronizar con el backend para asegurar que tenemos todos los pulls actuales
            try {
                val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
                val syncResp = service.getPullsByRole(auth, role = "buyer", userId = buyerId)
                
                if (syncResp.isSuccessful) {
                    val bodyStr = syncResp.body()?.string().orEmpty()
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

                    val listType = object : TypeToken<List<PullDto>>() {}.type
                    val dtoList: List<PullDto> = gson.fromJson(itemsArray.toString(), listType)

                    val pulls = dtoList.map { dto ->
                        Pull(
                            id = dto.id,
                            sellerId = dto.sellerId,
                            buyerId = dto.buyerId,
                            gigId = dto.gigId,
                            priceInit = dto.priceInit,
                            priceUpdate = dto.priceUpdate,
                            state = PullState.fromString(dto.state)
                        )
                    }
                    pullDao.insertAll(pulls.map { it.toEntity() })
                }
            } catch (e: Exception) {
                // Si falla la sincronización, continuar con la verificación local
            }

            // Verificar si ya existe un pull para este gig y buyer
            val existingPull = pullDao.findByGigAndBuyer(gigId, buyerId)
            if (existingPull != null) {
                return@withContext Resource.Error("Ya tienes un pull activo para este Gig (Pull #${existingPull.id})")
            }

            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            val request = CreatePullRequest(
                sellerId = sellerId,
                gigId = gigId,
                priceInit = priceInit,
                priceUpdate = priceUpdate,
                buyerId = buyerId,
                state = PullState.toString(state)
            )
            val resp = service.createPull(auth, request)

            if (resp.isSuccessful && resp.body() != null) {
                val dto = resp.body()!!
                val pull = Pull(
                    id = dto.id,
                    sellerId = dto.sellerId,
                    gigId = dto.gigId,
                    priceInit = dto.priceInit,
                    priceUpdate = dto.priceUpdate,
                    buyerId = dto.buyerId,
                    state = PullState.fromString(dto.state)
                )
                // Guardar en cache local
                pullDao.insert(pull.toEntity())
                Resource.Success(pull)
            } else {
                val errorBody = resp.errorBody()?.string() ?: "Sin detalles"
                val errorMsg = when (resp.code()) {
                    404 -> "Endpoint no encontrado (404). Verifica la URL: api/v1/pull"
                    401 -> "No autorizado (401). Verifica el token"
                    400 -> {
                        // Si es un error 400, intentar extraer el pull existente del mensaje de error
                        if (errorBody.contains("already exists") || errorBody.contains("existente")) {
                            // Buscar el pull en el backend
                            val existingPullFromBackend = pullDao.findByGigAndBuyer(gigId, buyerId)
                            if (existingPullFromBackend != null) {
                                return@withContext Resource.Error("Ya tienes un pull activo para este Gig (Pull #${existingPullFromBackend.id})")
                            }
                        }
                        "Request inválido (400): $errorBody"
                    }
                    else -> "Error ${resp.code()}: $errorBody"
                }
                Resource.Error(errorMsg)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al crear pull")
        }
    }

    /**
     * Obtener todos los pulls
     */
    suspend fun getAllPulls(): Resource<List<Pull>> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            val resp = service.getPulls(auth)

            if (!resp.isSuccessful) {
                val local = pullDao.getAll().map { it.toDomain() }
                return@withContext if (local.isNotEmpty()) Resource.Success(local)
                else Resource.Error("Error ${resp.code()}")
            }

            val bodyStr = resp.body()?.string().orEmpty()

            // Extraer el array de datos (igual que en GigRepository)
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

            val listType = object : TypeToken<List<PullDto>>() {}.type
            val dtoList: List<PullDto> = gson.fromJson(itemsArray.toString(), listType)

            val pulls = dtoList.map { dto ->
                Pull(
                    id = dto.id,
                    sellerId = dto.sellerId,
                    gigId = dto.gigId,
                    priceInit = dto.priceInit,
                    priceUpdate = dto.priceUpdate,
                    buyerId = dto.buyerId,
                    state = PullState.fromString(dto.state)
                )
            }

            // Cache en Room
            pullDao.clearAll()
            pullDao.insertAll(pulls.map { it.toEntity() })

            Resource.Success(pulls)
        } catch (e: Exception) {
            val local = pullDao.getAll().map { it.toDomain() }
            if (local.isNotEmpty()) Resource.Success(local)
            else Resource.Error(e.message ?: "Error al obtener pulls")
        }
    }

    /**
     * Obtener un pull por ID
     */
    suspend fun getPullById(id: Int): Resource<Pull> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            val resp = service.getPullById(id, auth)
            if (resp.isSuccessful && resp.body() != null) {
                val dto = resp.body()!!
                val pull = Pull(
                    id = dto.id,
                    sellerId = dto.sellerId,
                    gigId = dto.gigId,
                    priceInit = dto.priceInit,
                    priceUpdate = dto.priceUpdate,
                    buyerId = dto.buyerId,
                    state = PullState.fromString(dto.state)
                )
                Resource.Success(pull)
            } else {
                // Fallback a cache
                val local = pullDao.getById(id)?.toDomain()
                if (local != null) Resource.Success(local)
                else Resource.Error("Error ${resp.code()}")
            }
        } catch (e: Exception) {
            val local = pullDao.getById(id)?.toDomain()
            if (local != null) Resource.Success(local)
            else Resource.Error(e.message ?: "Error al obtener detalle del pull")
        }
    }

    /**
     * Obtener pulls por buyerId usando el endpoint by-role
     */
    suspend fun getPullsByBuyerId(buyerId: Int): Resource<List<Pull>> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            val resp = service.getPullsByRole(auth, role = "buyer", userId = buyerId)

            if (!resp.isSuccessful) {
                val local = pullDao.getByBuyerId(buyerId).map { it.toDomain() }
                return@withContext if (local.isNotEmpty()) Resource.Success(local)
                else Resource.Error("Error ${resp.code()}")
            }

            val bodyStr = resp.body()?.string().orEmpty()

            // Extraer el array de datos
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

            val listType = object : TypeToken<List<PullDto>>() {}.type
            val dtoList: List<PullDto> = gson.fromJson(itemsArray.toString(), listType)

            val pulls = dtoList.map { dto ->
                Pull(
                    id = dto.id,
                    sellerId = dto.sellerId,
                    buyerId = dto.buyerId,
                    gigId = dto.gigId,
                    priceInit = dto.priceInit,
                    priceUpdate = dto.priceUpdate,
                    state = PullState.fromString(dto.state)
                )
            }

            // Cache en Room
            pullDao.insertAll(pulls.map { it.toEntity() })

            Resource.Success(pulls)
        } catch (e: Exception) {
            val local = pullDao.getByBuyerId(buyerId).map { it.toDomain() }
            if (local.isNotEmpty()) Resource.Success(local)
            else Resource.Error(e.message ?: "Error al obtener pulls")
        }
    }

    /**
     * Actualizar un pull (precio y estado)
     */
    suspend fun updatePull(
        id: Int,
        newPrice: Double,
        newState: PullState
    ): Resource<Pull> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            val request = UpdatePullRequest(
                newPrice = newPrice,
                newState = PullState.toString(newState)
            )
            val resp = service.updatePull(id, auth, request)

            if (resp.isSuccessful && resp.body() != null) {
                val dto = resp.body()!!
                val pull = Pull(
                    id = dto.id,
                    sellerId = dto.sellerId,
                    gigId = dto.gigId,
                    priceInit = dto.priceInit,
                    priceUpdate = dto.priceUpdate,
                    buyerId = dto.buyerId,
                    state = PullState.fromString(dto.state)
                )
                pullDao.insert(pull.toEntity())
                Resource.Success(pull)
            } else {
                Resource.Error("Error al actualizar pull: ${resp.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar pull")
        }
    }

    /**
     * Cerrar un pull (cambia el estado a complete)
     */
    suspend fun closePull(id: Int): Resource<Pull> = withContext(Dispatchers.IO) {
        try {
            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            val resp = service.closePull(id, auth)

            if (resp.isSuccessful && resp.body() != null) {
                val dto = resp.body()!!
                val pull = Pull(
                    id = dto.id,
                    sellerId = dto.sellerId,
                    buyerId = dto.buyerId,
                    gigId = dto.gigId,
                    priceInit = dto.priceInit,
                    priceUpdate = dto.priceUpdate,
                    state = PullState.fromString(dto.state)
                )
                pullDao.insert(pull.toEntity())
                Resource.Success(pull)
            } else {
                Resource.Error("Error al cerrar pull: ${resp.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al cerrar pull")
        }
    }
}

