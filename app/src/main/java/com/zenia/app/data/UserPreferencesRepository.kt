package com.zenia.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserPreferencesRepository(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        private val ALLOW_WEAK_BIOMETRICS = booleanPreferencesKey("allow_weak_biometrics")
    }

    val isBiometricEnabled: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] ?: false
        }

    val allowWeakBiometrics: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[ALLOW_WEAK_BIOMETRICS] ?: false
        }

    suspend fun setBiometricEnabled(isEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[IS_BIOMETRIC_ENABLED] = isEnabled
        }
    }

    suspend fun saveAllowWeakBiometrics(allow: Boolean) {
        dataStore.edit { preferences ->
            preferences[ALLOW_WEAK_BIOMETRICS] = allow
        }
    }
}