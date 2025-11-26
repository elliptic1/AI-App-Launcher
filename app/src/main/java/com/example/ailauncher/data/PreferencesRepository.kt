package com.example.ailauncher.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ailauncher.model.ChatMessage
import com.example.ailauncher.model.ChatRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ai_launcher_prefs")

class PreferencesRepository(private val context: Context) {
    private val densityKey = intPreferencesKey("layout_density")
    private val lastGreetingKey = stringPreferencesKey("last_greeting")

    val layoutDensity: Flow<Int?> = context.dataStore.data.map { it[densityKey] }
    val lastGreeting: Flow<String?> = context.dataStore.data.map { it[lastGreetingKey] }

    suspend fun saveLayoutDensity(value: Int) {
        context.dataStore.edit { prefs ->
            prefs[densityKey] = value
        }
    }

    suspend fun saveLastGreeting(value: String) {
        context.dataStore.edit { prefs ->
            prefs[lastGreetingKey] = value
        }
    }

    suspend fun persistChat(chat: List<ChatMessage>) {
        // Placeholder for more robust storage. No-op to keep the stub simple.
    }

    fun loadChat(): Flow<List<ChatMessage>> = context.dataStore.data.map {
        emptyList()
    }
}
