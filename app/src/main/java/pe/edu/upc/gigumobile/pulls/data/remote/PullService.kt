package pe.edu.upc.gigumobile.pulls.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface PullService {

    @POST("api/Pull")
    suspend fun createPull(
        @Header("Authorization") auth: String?,
        @Body request: CreatePullRequest
    ): Response<PullDto>

    @GET("api/Pull")
    suspend fun getPulls(
        @Header("Authorization") auth: String?
    ): Response<ResponseBody>

    @GET("api/Pull/{id}")
    suspend fun getPullById(
        @Path("id") id: Int,
        @Header("Authorization") auth: String?
    ): Response<PullDto>

    @GET("api/Pull/by-role")
    suspend fun getPullsByRole(
        @Header("Authorization") auth: String?,
        @Query("role") role: String,
        @Query("userId") userId: Int
    ): Response<ResponseBody>

    @PUT("api/Pull/{id}")
    suspend fun updatePull(
        @Path("id") id: Int,
        @Header("Authorization") auth: String?,
        @Body request: UpdatePullRequest
    ): Response<PullDto>

    @PUT("api/Pull/{id}/close")
    suspend fun closePull(
        @Path("id") id: Int,
        @Header("Authorization") auth: String?
    ): Response<PullDto>
}

