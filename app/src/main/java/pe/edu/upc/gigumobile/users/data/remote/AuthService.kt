package pe.edu.upc.gigumobile.users.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("api/v1/User/sign-up")
    suspend fun signUp(@Body request: SignUpRequest): Response<ResponseBody>

    @POST("api/v1/User/login")
    suspend fun login(@Body request: LoginRequest): Response<ResponseBody>

    @POST("api/v1/User/google-login")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<ResponseBody>
}
