package pe.edu.upc.gigumobile.users.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pe.edu.upc.gigumobile.common.Resource
import pe.edu.upc.gigumobile.users.data.local.UserDao
import pe.edu.upc.gigumobile.users.data.local.UserEntity
import pe.edu.upc.gigumobile.users.data.remote.AuthService
import pe.edu.upc.gigumobile.users.data.remote.LoginRequest
import pe.edu.upc.gigumobile.users.data.remote.SignUpRequest
import com.google.gson.JsonParser
import pe.edu.upc.gigumobile.common.SessionManager
import pe.edu.upc.gigumobile.users.domain.model.User

class UserRepository(
    private val service: AuthService,
    private val dao: UserDao,
    private val context: Context
    //private val sessionManager: SessionManager
) {
    // Internally create SessionManager
    private val sessionManager = SessionManager(context)

    suspend fun signUp(request: SignUpRequest): Resource<Unit> = withContext(Dispatchers.IO) {
        try {
            val res = service.signUp(request)
            if (res.isSuccessful) {
                Resource.Success(Unit)
            } else {
                val err = try { res.errorBody()?.string() } catch (e: Exception) { res.message() }
                Resource.Error("Sign-up failed: ${res.code()} - ${err ?: res.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error in signUp")
        }
    }

    suspend fun login(request: LoginRequest): Resource<String> = withContext(Dispatchers.IO) {
        try {
            val res = service.login(request)
            if (res.isSuccessful) {
                val raw = try { res.body()?.string() } catch (e: Exception) { null }
                val token = parseTokenFromRaw(raw)
                if (!token.isNullOrBlank()) {
                    val userEntity = UserEntity(email = request.email, token = token)
                    dao.insert(userEntity)
                    sessionManager.saveToken(token) // Save the session
                    Resource.Success(token)
                } else {
                    Resource.Error("Empty token from server")
                }
            } else {
                val err = try { res.errorBody()?.string() } catch (e: Exception) { res.message() }
                Resource.Error("Login failed: ${res.code()} - ${err ?: res.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error in login")
        }
    }

    private fun parseTokenFromRaw(raw: String?): String? {
        if (raw == null) return null
        val trimmed = raw.trim()
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length > 1) {
            return trimmed.substring(1, trimmed.length - 1)
        }
        return try {
            val json = JsonParser.parseString(trimmed).asJsonObject
            when {
                json.has("token") -> json.get("token").asString
                json.has("jwt") -> json.get("jwt").asString
                json.has("access_token") -> json.get("access_token").asString
                else -> null
            }
        } catch (_: Exception) {
            trimmed
        }
    }

    // Test 1
    //suspend fun getSavedUser() = withContext(Dispatchers.IO) {
    //    dao.fetchAny()
    //}

    //suspend fun clearUser() = withContext(Dispatchers.IO) {
    //    dao.clearAll()
    //}

    // Test 2
    //suspend fun getSavedUser(): User? {
    //    val entity = userDao.getUser()
    //    return entity?.toDomain()
    //}
//
    //suspend fun clearSession() {
    //    sessionManager.clearSession()
    //    userDao.deleteAll()
    //}

    // Test 3
    suspend fun getSavedUser(): User? {
        val entity = dao.fetchAny()
        return entity?.toDomain()
    }

    suspend fun clearSession() {
        sessionManager.clearSession()
        dao.clearAll()
    }

    // User Mapper
    fun UserEntity.toDomain(): User {
        return User(
            email = this.email,
            name = this.name,
            lastname = this.lastname,
            role = this.role,
            image = this.image
        )
    }
}
