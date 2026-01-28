package com.zenia.app.di

import com.zenia.app.domain.security.BiometricAuthenticator
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BiometricEntryPoint {
    fun biometricAuthenticator(): BiometricAuthenticator
}