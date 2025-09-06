package com.particleswe.esggt7.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.particleswe.esggt7.data.Car
import com.particleswe.esggt7.network.EngineSwapRepository
import com.particleswe.esggt7.storage.FavoritesDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EngineSwapViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = EngineSwapRepository()
    private val favoritesStore = FavoritesDataStore(app.applicationContext)

    /** All cars as (brand, car) pairs */
    private val _cars = MutableStateFlow<List<Pair<String, Car>>>(emptyList())
    val cars: StateFlow<List<Pair<String, Car>>> = _cars.asStateFlow()

    /** Search */
    val searchQuery = MutableStateFlow("")

    /** Favorites as set of keys "Brand|Car" */
    private val _favorites: StateFlow<Set<String>> =
        favoritesStore.favoritesFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptySet()
        )
    val favorites: StateFlow<Set<String>> = _favorites

    init {
        fetchData()
    }

    private fun fetchData() {
        viewModelScope.launch {
            try {
                _cars.value = repository.getAllCars()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    /** Compose a stable key for a car */
    fun carKey(brand: String, car: Car): String = "$brand|${car.car}"

    fun isFavorite(brand: String, car: Car): Boolean =
        _favorites.value.contains(carKey(brand, car))

    fun toggleFavorite(brand: String, car: Car) {
        viewModelScope.launch {
            favoritesStore.toggleFavorite(carKey(brand, car))
        }
    }
}





