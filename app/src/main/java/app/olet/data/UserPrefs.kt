package app.olet.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("olet_prefs")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class UserProfile(
    val loggedIn: Boolean = false,
    val name: String = "",
    val email: String = "",
    val isGoogle: Boolean = false,
    val currency: String = "৳",
    val theme: ThemeMode = ThemeMode.SYSTEM,
)

class UserPrefs(private val context: Context) {
    private object K {
        val loggedIn = booleanPreferencesKey("logged_in")
        val name = stringPreferencesKey("name")
        val email = stringPreferencesKey("email")
        val google = booleanPreferencesKey("google")
        val currency = stringPreferencesKey("currency")
        val theme = stringPreferencesKey("theme")
    }

    val profile: Flow<UserProfile> = context.dataStore.data.map { p ->
        UserProfile(
            loggedIn = p[K.loggedIn] ?: false,
            name = p[K.name] ?: "",
            email = p[K.email] ?: "",
            isGoogle = p[K.google] ?: false,
            currency = p[K.currency] ?: "৳",
            theme = p[K.theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
        )
    }

    suspend fun signIn(name: String, email: String, google: Boolean) {
        context.dataStore.edit {
            it[K.loggedIn] = true
            it[K.name] = name
            it[K.email] = email
            it[K.google] = google
        }
    }

    suspend fun signOut() {
        context.dataStore.edit {
            it[K.loggedIn] = false
            it.remove(K.name); it.remove(K.email); it.remove(K.google)
        }
    }

    suspend fun updateName(name: String) = context.dataStore.edit { it[K.name] = name }
    suspend fun setCurrency(symbol: String) = context.dataStore.edit { it[K.currency] = symbol }
    suspend fun setTheme(mode: ThemeMode) = context.dataStore.edit { it[K.theme] = mode.name }
}
