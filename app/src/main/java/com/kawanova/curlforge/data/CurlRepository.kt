package com.kawanova.curlforge.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "curlforge_settings")

class CurlRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val KEY_SAVED_SNIPPETS = stringPreferencesKey("saved_snippets")
        private val KEY_LAST_CURL = stringPreferencesKey("last_curl_command")
        private val KEY_LAST_TARGET_LANG = stringPreferencesKey("last_target_language")
    }

    val savedSnippetsFlow: Flow<List<SavedSnippet>> = context.dataStore.data.map { prefs ->
        val raw = prefs[KEY_SAVED_SNIPPETS]
        if (raw.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                json.decodeFromString<List<SavedSnippet>>(raw)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun saveSnippet(snippet: SavedSnippet) {
        context.dataStore.edit { prefs ->
            val currentRaw = prefs[KEY_SAVED_SNIPPETS]
            val currentList = if (currentRaw.isNullOrBlank()) {
                emptyList()
            } else {
                try {
                    json.decodeFromString<List<SavedSnippet>>(currentRaw)
                } catch (e: Exception) {
                    emptyList()
                }
            }.toMutableList()

            // 既存にあれば更新、なければ先頭に追加
            val index = currentList.indexOfFirst { it.id == snippet.id }
            if (index != -1) {
                currentList[index] = snippet
            } else {
                currentList.add(0, snippet)
            }
            prefs[KEY_SAVED_SNIPPETS] = json.encodeToString(currentList)
        }
    }

    suspend fun deleteSnippet(id: String) {
        context.dataStore.edit { prefs ->
            val currentRaw = prefs[KEY_SAVED_SNIPPETS] ?: return@edit
            val currentList = try {
                json.decodeFromString<List<SavedSnippet>>(currentRaw)
            } catch (e: Exception) {
                emptyList()
            }.toMutableList()

            currentList.removeAll { it.id == id }
            prefs[KEY_SAVED_SNIPPETS] = json.encodeToString(currentList)
        }
    }

    val lastCurlFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_CURL]
    }

    suspend fun setLastCurl(curl: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_CURL] = curl
        }
    }

    val lastTargetLangFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_LAST_TARGET_LANG]
    }

    suspend fun setLastTargetLang(langName: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_LAST_TARGET_LANG] = langName
        }
    }
}
