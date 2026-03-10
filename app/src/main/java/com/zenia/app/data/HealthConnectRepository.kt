package com.zenia.app.data

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.zenia.app.di.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HealthSummary(
    val heartRateAvg: Int?,
    val sleepHours: Float,
    val steps: Long,
    val stressLevel: String
)

class HealthConnectRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {

    val sdkStatus: Int = HealthConnectClient.getSdkStatus(context)

    private val client: HealthConnectClient? =
        when (sdkStatus) {
            HealthConnectClient.SDK_AVAILABLE ->
                HealthConnectClient.getOrCreate(context)

            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                null
            }

            else -> null
        }

    val isAvailable: Boolean
        get() = client != null

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class)
    )

    fun permissionContract(): ActivityResultContract<Set<String>, Set<String>> =
        PermissionController.createRequestPermissionResultContract()

    private fun getTimeRangeForDate(date: LocalDate): TimeRangeFilter {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        return TimeRangeFilter.between(startOfDay, endOfDay)
    }

    suspend fun readStepsByDate(date: LocalDate): Int = withContext(defaultDispatcher) {
        if (client == null || !hasPermissions()) return@withContext 0

        val records = client.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                getTimeRangeForDate(date)
            )
        ).records

        records.sumOf { it.count }.toInt()
    }

    suspend fun readSleepMinutesByDate(date: LocalDate): Int = withContext(defaultDispatcher) {
        if (client == null || !hasPermissions()) return@withContext 0

        val records = client.readRecords(
            ReadRecordsRequest(
                SleepSessionRecord::class,
                getTimeRangeForDate(date)
            )
        ).records

        records.sumOf {
            ChronoUnit.MINUTES.between(it.startTime, it.endTime)
        }.toInt()
    }

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
        if (samples.isEmpty()) null
        else samples.map { it.beatsPerMinute }.average().toInt()
    }

    suspend fun readTodaySteps(): Long = withContext(defaultDispatcher) {
        val now = Instant.now()
        val records = client?.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                TimeRangeFilter.between(now.minus(1, ChronoUnit.DAYS), now)
            )
        )?.records ?: return@withContext 0L

        records.sumOf { it.count }
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

    suspend fun readLatestHRV(): Double? = withContext(defaultDispatcher) {
        val now = Instant.now()
        val records = client?.readRecords(
            ReadRecordsRequest(
                HeartRateVariabilityRmssdRecord::class,
                TimeRangeFilter.between(now.minus(1, ChronoUnit.DAYS), now)
            )
        )?.records ?: return@withContext null

        records.lastOrNull()?.heartRateVariabilityMillis
    }

    suspend fun estimateStressLevel(): String {
        val hrv = readLatestHRV() ?: return "Desconocido"

        return when {
            hrv > 50 -> "Bajo"
            hrv > 30 -> "Moderado"
            else -> "Alto"
        }
    }

    suspend fun getHealthSummary(): HealthSummary = withContext(defaultDispatcher) {
        val heartRate = readHeartRateAvg()
        val sleep = readSleepHours()
        val steps = readTodaySteps()
        val stress = estimateStressLevel()

        HealthSummary(
            heartRateAvg = heartRate,
            sleepHours = sleep,
            steps = steps,
            stressLevel = stress
        )
    }
}