package com.example.healthsync.backend.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface UsdaApiService {
    @GET("foods/search")
    suspend fun searchFood(
        @Query("query") query: String,
        @Query("pageSize") pageSize: Int,
        @Query("api_key") apiKey: String
    ): UsdaSearchResponse
}
