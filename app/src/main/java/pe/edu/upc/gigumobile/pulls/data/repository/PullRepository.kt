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
                val token = userDao.fetchAny()?.token ?: return@withContext 0
                
                // Decodificar el JWT token para extraer el userId
                val parts = token.split(".")
                if (parts.size != 3) {
                    // Token inválido - no retornar valor por defecto
                    return@withContext 0
                }
                
                // Decodificar el payload (segunda parte del JWT)
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val jsonPayload = JSONObject(payload)
                
                // Extraer el sid (user ID) del claim
                val sidClaim = "http://schemas.xmlsoap.org/ws/2005/05/identity/claims/sid"
                if (jsonPayload.has(sidClaim)) {
                    val userId = jsonPayload.getString(sidClaim).toIntOrNull()
                    // Solo retornar si el userId es válido (mayor que 0)
                    userId?.takeIf { it > 0 } ?: 0
                } else {
                    // No se encontró el claim - no retornar valor por defecto
                    0
                }
            } catch (e: Exception) {
                // Si hay error al decodificar, retornar 0 para indicar error
                // NO retornar 1 como fallback porque causaría que todos los usuarios
                // con problemas de token tengan el mismo buyerId
                0
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
            // Validar que los parámetros sean válidos
            if (gigId <= 0) {
                return@withContext Resource.Error("El ID del gig no es válido. No se puede crear el pull.")
            }
            if (buyerId <= 0) {
                return@withContext Resource.Error("El ID del buyer no es válido. No se puede crear el pull. Por favor, cierra sesión y vuelve a iniciar sesión.")
            }
            if (sellerId <= 0) {
                return@withContext Resource.Error("El ID del seller no es válido. No se puede crear el pull.")
            }
            
            val auth = authHeader() ?: return@withContext Resource.Error("Debes iniciar sesión.")
            
            // VALIDACIÓN OBLIGATORIA CONTRA EL BACKEND
            // IMPORTANTE: Obtener el buyerId real del token para asegurar que estamos usando el ID correcto
            val actualBuyerId = getCurrentUserId()
            if (actualBuyerId <= 0) {
                return@withContext Resource.Error("No se pudo identificar tu usuario. Por favor, cierra sesión y vuelve a iniciar sesión.")
            }
            
            // Usar el buyerId obtenido del token, no el que se pasó como parámetro
            // Esto previene problemas cuando el buyerId pasado es incorrecto
            val buyerIdToUse = actualBuyerId
            
            // Consultar al backend para obtener todos los pulls del buyer y verificar si el gigId ya existe
            val checkResp = service.getPullsByRole(auth, role = "buyer", userId = buyerIdToUse)
            
            if (!checkResp.isSuccessful) {
                return@withContext Resource.Error("No se pudo verificar los pulls existentes. Error ${checkResp.code()}")
            }
            
            val bodyStr = checkResp.body()?.string().orEmpty()
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
            val dtoList: List<PullDto> = try {
                gson.fromJson(itemsArray.toString(), listType)
            } catch (e: Exception) {
                // Si falla el parsing, asumir que no hay pulls (array vacío)
                emptyList()
            }

            // Filtrar solo los pulls que pertenecen a este buyer específico
            // (por si el backend devuelve pulls de otros buyers por error)
            val buyerPulls = dtoList.filter { dto -> 
                dto.buyerId == buyerIdToUse 
            }

            // Verificar si el gigId ya existe en algún pull de este buyer
            // IMPORTANTE: Solo bloqueamos si el gigId es EXACTAMENTE el mismo
            // Esto permite que un buyer cree múltiples pulls de diferentes gigs
            val existingPullInBackend = buyerPulls.firstOrNull { dto ->
                // Comparación estricta: debe coincidir EXACTAMENTE el gigId
                // Si los gigIds son diferentes, esta validación NO debe bloquear
                val matches = dto.gigId == gigId
                matches
            }
            
            if (existingPullInBackend != null) {
                // Ya existe un pull para este gigId específico con este buyer
                // Esto es correcto: un buyer no puede crear múltiples pulls del mismo gig
                // Pero SÍ puede crear pulls de diferentes gigs
                // Incluir información de depuración: mostrar todos los gigIds de los pulls existentes
                val existingGigIds = buyerPulls.map { it.gigId }.joinToString(", ")
                return@withContext Resource.Error(
                    "Ya generaste un pull para este gig (Pull #${existingPullInBackend.id}, GigId: ${existingPullInBackend.gigId}). " +
                    "Intentando crear pull para GigId: $gigId. " +
                    "Pulls existentes (GigIds): [$existingGigIds]. " +
                    "Puedes crear pulls de otros gigs diferentes."
                )
            }
            
            // Si llegamos aquí, NO existe un pull con este gigId para este buyer
            // Proceder a crear el nuevo pull

            // Reutilizar auth ya declarado arriba
            val request = CreatePullRequest(
                sellerId = sellerId,
                gigId = gigId,
                priceInit = priceInit,
                priceUpdate = priceUpdate,
                buyerId = buyerIdToUse, // Usar el buyerId obtenido del token
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
                // Actualizar cache local solo con el pull recién creado (para optimización offline)
                // La cache es solo para mostrar datos, no para validaciones de negocio
                pullDao.insert(pull.toEntity())
                Resource.Success(pull)
            } else {
                val errorBody = resp.errorBody()?.string() ?: "Sin detalles"
                val errorMsg = when (resp.code()) {
                    404 -> "Endpoint no encontrado (404). Verifica la URL: api/Pull"
                    401 -> "No autorizado (401). Verifica el token"
                    400 -> {
                        // Si es un error 400, puede ser que el backend rechace porque ya existe
                        if (errorBody.contains("already exists") || errorBody.contains("existente") || errorBody.contains("ya existe")) {
                            "Ya existe un pull para este gig. No se puede crear otro."
                        } else {
                            "Request inválido (400): $errorBody"
                        }
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

            // Actualizar cache local solo para optimización de visualización (no para validaciones)
            pullDao.clearAll()
            pullDao.insertAll(pulls.map { it.toEntity() })

            Resource.Success(pulls)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener pulls desde el backend")
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
                Resource.Error("Error al obtener pull: ${resp.code()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener detalle del pull desde el backend")
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
                return@withContext Resource.Error("Error al obtener pulls: ${resp.code()}")
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

            // Actualizar cache local solo para optimización de visualización (no para validaciones)
            pullDao.insertAll(pulls.map { it.toEntity() })

            Resource.Success(pulls)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener pulls desde el backend")
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

