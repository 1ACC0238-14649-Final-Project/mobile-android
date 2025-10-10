package pe.edu.upc.gigumobile.gigs.data.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GigService {

    @GET("api/v1/Gig")
    suspend fun getGigsRaw(
        @Header("Authorization") auth: String?,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 10,
        @Query("searchTerm") searchTerm: String? = null,
        @Query("minPrice") minPrice: Double? = null,
        @Query("maxPrice") maxPrice: Double? = null,
        @Query("maxDeliveryDays") maxDeliveryDays: Int? = null,
        @Query("sortBy") sortBy: String = "createdAt",
        @Query("descending") descending: Boolean = true
    ): Response<ResponseBody>

    @GET("api/v1/Gig/{id}")
    suspend fun getGigDetail(
        @Path("id") id: String,
        @Header("Authorization") auth: String?
    ): Response<GigDto>
}
