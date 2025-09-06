package com.particleswe.esggt7

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import com.particleswe.esggt7.data.Car
import com.particleswe.esggt7.viewmodel.EngineSwapViewModel
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.ui.Alignment


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineSwapScreen(viewModel: EngineSwapViewModel = viewModel()) {
    val searchQuery = viewModel.searchQuery.collectAsState().value
    val carsState = viewModel.cars.collectAsState().value
    var selectedTab by remember { mutableStateOf(0) } // 0 Brand, 1 Engine, 2 Favorites
    val tabs = listOf("By Brand", "By Engine", "Favorites")
    val favorites = viewModel.favorites.collectAsState().value



    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF8B0000), // dark red top-left
            Color.White,        // middle
            Color(0xFF00008B)   // dark blue bottom-right
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(1000f, 2000f) // diagonal
    )


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = gradient)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        text = when (selectedTab) {
                            0 -> "Cars"
                            1 -> "Engines"
                            else -> "Favorites"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )


            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Search cars or engines...") },
                singleLine = true
            )

            TabRow(selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (selectedTab) {
                0 -> CarsByBrandTab(carsState, searchQuery, favorites, viewModel)
                1 -> CarsByEngineTab(carsState, searchQuery, favorites, viewModel)
                2 -> FavoritesTab(carsState, searchQuery, favorites, viewModel)
            }
        }
    }
}

@Composable
fun CarsByBrandTab(
    cars: List<Pair<String, Car>>,
    searchQuery: String,
    favorites: Set<String>,
    viewModel: EngineSwapViewModel) {
    val carsByBrand = remember(cars, searchQuery) {
        val filtered = if (searchQuery.isBlank()) {
            cars
        } else {
            cars.filter { (brand, car) ->
                brand.lowercase().contains(searchQuery.lowercase()) ||
                        car.car.lowercase().contains(searchQuery.lowercase()) ||
                        car.swappableEngines.any { it.lowercase().contains(searchQuery.lowercase()) }
            }
        }
        filtered.groupBy { it.first }.mapValues { it.value.map { pair -> pair.second } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        carsByBrand.forEach { (brand, cars) ->
            item {
                Text(
                    text = brand,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                )
            }
            items(cars) { car ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Header row with title + star
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                car.car,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            val key = viewModel.carKey(brand, car)
                            val fav = favorites.contains(key)
                            IconToggleButton(
                                checked = fav,
                                onCheckedChange = { viewModel.toggleFavorite(brand, car) }
                            ) {
                                if (fav) {
                                    Icon(Icons.Filled.Star, contentDescription = "Unfavorite")
                                } else {
                                    Icon(Icons.Outlined.StarBorder, contentDescription = "Favorite")
                                }
                            }
                        }
                        if (car.swappableEngines.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // Engines list (for brand tab)
                        car.swappableEngines.forEach { engine ->
                            Text(
                                "• $engine",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

            }
        }
    }
}

@Composable
fun CarsByEngineTab(
    cars: List<Pair<String, Car>>,
    searchQuery: String,
    favorites: Set<String>,
    viewModel: EngineSwapViewModel) {
    // In CarsByEngineTab:
    val engineTriples = remember(cars, searchQuery) {
        // (engine, brand, car)
        val pairs = cars.flatMap { (brand, car) ->
            car.swappableEngines.map { engine -> Triple(engine, brand, car) }
        }
        val filtered = if (searchQuery.isBlank()) pairs else pairs.filter { (engine, brand, car) ->
            engine.contains(searchQuery, ignoreCase = true) ||
                    brand.contains(searchQuery, ignoreCase = true) ||
                    car.car.contains(searchQuery, ignoreCase = true)
        }
        filtered.sortedBy { it.first.lowercase() } // sort by engine name
    }

// Group by engine
    val enginesGrouped: Map<String, List<Pair<String, Car>>> = remember(engineTriples) {
        engineTriples.groupBy({ it.first }) { it.second to it.third }
    }


    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        enginesGrouped.forEach { (engine, brandAndCars) ->
            item {
                Text(
                    text = engine,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp)
                )
            }
            items(brandAndCars) { (brand, car) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                car.car,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            val key = viewModel.carKey(brand, car)
                            val fav = favorites.contains(key)
                            IconToggleButton(
                                checked = fav,
                                onCheckedChange = { viewModel.toggleFavorite(brand, car)  }
                            ) {
                                if (fav) {
                                    Icon(Icons.Filled.Star, contentDescription = "Unfavorite")
                                } else {
                                    Icon(Icons.Outlined.StarBorder, contentDescription = "Favorite")
                                }
                            }
                        }

                    }
                }
            }
        }

    }
}

@Composable
fun FavoritesTab(
    cars: List<Pair<String, Car>>,
    searchQuery: String,
    favorites: Set<String>,
    viewModel: EngineSwapViewModel
) {
    val favorites = viewModel.favorites.collectAsState().value

    // Keep only favorited (brand, car)
    val favored = remember(cars, favorites, searchQuery) {
        val filtered = cars.filter { (brand, car) ->
            favorites.contains(viewModel.carKey(brand, car))
        }
        if (searchQuery.isBlank()) filtered
        else filtered.filter { (brand, car) ->
            brand.contains(searchQuery, true) ||
                    car.car.contains(searchQuery, true) ||
                    car.swappableEngines.any { it.contains(searchQuery, true) }
        }
    }

    // Group favorites by brand for a nice layout
    val grouped = remember(favored) {
        favored.groupBy({ it.first }) { it.second }
            .toSortedMap(String.CASE_INSENSITIVE_ORDER)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 12.dp)
    ) {
        if (grouped.isEmpty()) {
            item {
                Text(
                    "No favorites yet. Tap the star on any car to add it here.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            grouped.forEach { (brand, carsList) ->
                item {
                    Text(
                        text = brand,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 12.dp)
                    )
                }
                items(carsList) { car ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    car.car,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                val key = viewModel.carKey(brand, car)
                                val fav = favorites.contains(key)
                                IconToggleButton(
                                    checked = fav,
                                    onCheckedChange = { viewModel.toggleFavorite(brand, car)  }
                                ) {
                                    if (fav) {
                                        Icon(Icons.Filled.Star, contentDescription = "Unfavorite")
                                    } else {
                                        Icon(Icons.Outlined.StarBorder, contentDescription = "Favorite")
                                    }
                                }
                            }
                            if (car.swappableEngines.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                            }

                            car.swappableEngines.forEach { engine ->
                                Text(
                                    "• $engine",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

