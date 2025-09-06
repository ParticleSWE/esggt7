package com.particleswe.esggt7.data

data class EngineSwapDatabase(
    val carsByBrand: Map<String, List<Car>>
)

data class Car(
    val car: String,
    val swappableEngines: List<String>
)
