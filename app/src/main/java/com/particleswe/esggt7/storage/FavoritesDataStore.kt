package com.particleswe.esggt7.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DS_NAME = "favorites"
private val Context.dataStore by preferencesDataStore(DS_NAME)

private val FAVORITES_KEY = stringSetPreferencesKey("favorite_keys")

/** Keys are strings like "Brand|Car Name" */
class FavoritesDataStore(private val context: Context) {

    val favoritesFlow: Flow<Set<String>> =
        context.dataStore.data.map { it[FAVORITES_KEY] ?: emptySet() }

    suspend fun toggleFavorite(key: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY] ?: emptySet()
            prefs[FAVORITES_KEY] = if (current.contains(key)) {
                current - key
            } else {
                current + key
            }
        }
    }
}
