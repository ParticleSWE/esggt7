package com.particleswe.esggt7.network

import com.particleswe.esggt7.data.Car

class EngineSwapRepository {

    private val api = RetrofitInstance.api

    suspend fun getAllCars(): List<Pair<String, Car>> {
        val response = api.getEngineSwaps()
        // Flatten the map into a list of (brand, car) pairs
        return response.carsByBrand.flatMap { (brand, cars) ->
            cars.map { car -> brand to car }
        }
    }
}

