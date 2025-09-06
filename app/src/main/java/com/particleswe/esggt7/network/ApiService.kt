package com.particleswe.esggt7.network

import com.particleswe.esggt7.data.EngineSwapDatabase
import retrofit2.http.GET

interface ApiService {
    @GET("engine_swap_database.json")
    suspend fun getEngineSwaps(): EngineSwapDatabase
}
