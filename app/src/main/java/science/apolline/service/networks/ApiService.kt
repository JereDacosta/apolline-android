package science.apolline.service.networks

import retrofit2.Call
import retrofit2.http.*
import retrofit2.http.POST
import science.apolline.models.InfluxBody


/**
 * Created by sparow on 13/10/2017.
 */

interface ApiService {
    @POST("write")
    fun savePost(@Query("db") dbName: String,
                 @Query("u") dbUserName: String,
                 @Query("p") dbPassword: String,
                 @Body data: String): Call<InfluxBody>
}