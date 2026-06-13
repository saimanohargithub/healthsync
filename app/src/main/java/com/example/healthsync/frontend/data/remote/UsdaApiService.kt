package com.example.healthsync.frontend.data.remote;

import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApiService {
    @GET("foods/search")
    suspend fun searchFood(
        @Query("query") query: String,
        @Query("pageSize") pageSize: Int = 1,
        @Query("api_key") apiKey: String
    ): FoodSearchResponse
}
