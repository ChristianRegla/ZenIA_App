package com.zenia.app.data

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.zenia.app.di.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class HealthConnectRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val client: HealthConnectClient? =
        if (HealthConnectClient.getSdkStatus(context) == HealthConnectClient.SDK_AVAILABLE)
            HealthConnectClient.getOrCreate(context)
        else null

    val isAvailable: Boolean get() = client != null

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class)
    )

    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    suspend fun hasPermissions(): Boolean {
        val granted = client?.permissionController?.getGrantedPermissions() ?: emptySet()
        return granted.containsAll(permissions)
    }

    suspend fun readHeartRateAvg(): Int? = withContext(defaultDispatcher) {
        val now = Instant.now()
        val records = client?.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                TimeRangeFilter.between(now.minus(1, ChronoUnit.DAYS), now)
            )
        )?.records ?: return@withContext null

        val samples = records.flatMap { it.samples }
        if (samples.isEmpty()) null else samples.map { it.beatsPerMinute }.average().toInt()
    }

    suspend fun readSleepHours(): Float = withContext(defaultDispatcher) {
        val now = Instant.now()
        val records = client?.readRecords(
            ReadRecordsRequest(
                SleepSessionRecord::class,
                TimeRangeFilter.between(now.minus(1, ChronoUnit.DAYS), now)
            )
        )?.records ?: return@withContext 0f

        records.sumOf {
            ChronoUnit.MINUTES.between(it.startTime, it.endTime)
        } / 60f
    }

    suspend fun estimateStressLevel(): String {
        val hr = readHeartRateAvg() ?: return "Desconocido"
        return when {
            hr < 65 -> "Bajo"
            hr < 85 -> "Moderado"
            else -> "Alto"
        }
    }
}