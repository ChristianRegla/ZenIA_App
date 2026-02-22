package com.zenia.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    private object Keys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val ALLOW_WEAK_BIOMETRICS = booleanPreferencesKey("allow_weak_biometrics")
        val HAS_SEEN_EXPORT_TUTORIAL = booleanPreferencesKey("has_seen_export_tutorial")
    }

    private val safeData = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences())
            else throw exception
        }

    val isOnboardingCompleted: Flow<Boolean> =
        safeData.map { it[Keys.ONBOARDING_COMPLETED] ?: false }

    val isBiometricEnabled: Flow<Boolean> =
        safeData.map { it[Keys.IS_BIOMETRIC_ENABLED] ?: false }

    val allowWeakBiometrics: Flow<Boolean> =
        safeData.map { it[Keys.ALLOW_WEAK_BIOMETRICS] ?: false }

    val hasSeenExportTutorial: Flow<Boolean> =
        safeData.map { it[Keys.HAS_SEEN_EXPORT_TUTORIAL] ?: false }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.IS_BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setAllowWeakBiometrics(allow: Boolean) {
        dataStore.edit { it[Keys.ALLOW_WEAK_BIOMETRICS] = allow }
    }

    suspend fun setExportTutorialSeen() {
        dataStore.edit { it[Keys.HAS_SEEN_EXPORT_TUTORIAL] = true }
    }
}