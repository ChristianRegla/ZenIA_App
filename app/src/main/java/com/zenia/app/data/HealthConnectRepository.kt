package com.zenia.app.data

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

data class HealthSummary(
    val heartRateAvg: Int?,
    val sleepHours: Float,
    val steps: Long,
    val stressLevel: String
)

data class SleepDuration(
    val hours: Int,
    val minutes: Int
) {
    val totalMinutes: Int get() = hours * 60 + minutes
}

sealed class HealthConnectAvailability {
    data object Available : HealthConnectAvailability()
    data object NotInstalledOrUpdateRequired : HealthConnectAvailability()
    data object NotSupported : HealthConnectAvailability()
}

sealed class HealthConnectNextStep {
    data object Ready : HealthConnectNextStep()
    data object RequestPermissions : HealthConnectNextStep()
    data object InstallOrUpdate : HealthConnectNextStep()
    data object NotSupported : HealthConnectNextStep()
}

class HealthConnectRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private val providerPackageName = "com.google.android.apps.healthdata"

    val sdkStatus: Int
        get() = HealthConnectClient.getSdkStatus(context, providerPackageName)

    private val client: HealthConnectClient?
        get() = when (sdkStatus) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectClient.getOrCreate(context)
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

    fun availability(): HealthConnectAvailability {
        return when (sdkStatus) {
            HealthConnectClient.SDK_AVAILABLE -> HealthConnectAvailability.Available
            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED ->
                HealthConnectAvailability.NotInstalledOrUpdateRequired
            else -> HealthConnectAvailability.NotSupported
        }
    }

    suspend fun getNextStep(): HealthConnectNextStep {
        return when (availability()) {
            HealthConnectAvailability.Available -> {
                if (hasPermissions()) HealthConnectNextStep.Ready
                else HealthConnectNextStep.RequestPermissions
            }

            HealthConnectAvailability.NotInstalledOrUpdateRequired ->
                HealthConnectNextStep.InstallOrUpdate

            HealthConnectAvailability.NotSupported ->
                HealthConnectNextStep.NotSupported
        }
    }

    private fun getTimeRangeForDate(date: LocalDate): TimeRangeFilter {
        val zoneId = ZoneId.systemDefault()
        val startOfDay = date.atStartOfDay(zoneId).toInstant()
        val endOfDay = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        return TimeRangeFilter.between(startOfDay, endOfDay)
    }

    private fun getTimeRangeForLastNightSleep(
        date: LocalDate,
        zoneId: ZoneId = ZoneId.systemDefault(),
        startTime: LocalTime = LocalTime.of(18, 0),
        endTime: LocalTime = LocalTime.of(12, 0)
    ): TimeRangeFilter {
        val start = date.minusDays(1).atTime(startTime).atZone(zoneId).toInstant()
        val end = date.atTime(endTime).atZone(zoneId).toInstant()
        return TimeRangeFilter.between(start, end)
    }

    suspend fun readStepsByDate(date: LocalDate): Long = withContext(defaultDispatcher) {
        val hc = client ?: return@withContext 0L
        if (!hasPermissions(hc)) return@withContext 0L

        val records = hc.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                getTimeRangeForDate(date)
            )
        ).records

        records.sumOf { it.count }
    }

    private suspend fun hasPermissions(hc: HealthConnectClient): Boolean {
        val granted = hc.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    suspend fun hasPermissions(): Boolean {
        val hc = client ?: return false
        return hasPermissions(hc)
    }

    suspend fun readHeartRateAvg(): Int? = withContext(defaultDispatcher) {
        val hc = client ?: return@withContext null
        if (!hasPermissions(hc)) return@withContext null

        val today = LocalDate.now()
        val records = hc.readRecords(
            ReadRecordsRequest(
                HeartRateRecord::class,
                getTimeRangeForDate(today)
            )
        ).records

        val samples = records.flatMap { it.samples }
        if (samples.isEmpty()) null
        else samples.map { it.beatsPerMinute }.average().toInt()
    }

    suspend fun readTodaySteps(): Long = withContext(defaultDispatcher) {
        val hc = client ?: return@withContext 0L
        if (!hasPermissions(hc)) return@withContext 0L

        val today = LocalDate.now()
        val records = hc.readRecords(
            ReadRecordsRequest(
                StepsRecord::class,
                getTimeRangeForDate(today)
            )
        ).records

        records.sumOf { it.count }
    }

    suspend fun readLatestHRV(): Double? = withContext(defaultDispatcher) {
        val hc = client ?: return@withContext null
        if (!hasPermissions(hc)) return@withContext null

        val today = LocalDate.now()
        val records = hc.readRecords(
            ReadRecordsRequest(
                HeartRateVariabilityRmssdRecord::class,
                getTimeRangeForDate(today)
            )
        ).records

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

    suspend fun readLastNightSleepDurationByDate(date: LocalDate): SleepDuration =
        withContext(defaultDispatcher) {
            val hc = client ?: return@withContext SleepDuration(0, 0)
            if (!hasPermissions(hc)) return@withContext SleepDuration(0, 0)

            val records = hc.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    getTimeRangeForLastNightSleep(date = date)
                )
            ).records

            val maxMinutes = records
                .maxOfOrNull { ChronoUnit.MINUTES.between(it.startTime, it.endTime) }
                ?.toInt()
                ?: 0

            SleepDuration(
                hours = maxMinutes / 60,
                minutes = maxMinutes % 60
            )
        }

    suspend fun readLastNightSleepDuration(): SleepDuration {
        return readLastNightSleepDurationByDate(LocalDate.now())
    }

    suspend fun getHealthSummary(): HealthSummary = withContext(defaultDispatcher) {
        val heartRate = readHeartRateAvg()

        val sleepDuration = readLastNightSleepDuration()
        val sleepHours = sleepDuration.totalMinutes / 60f

        val steps = readTodaySteps()
        val stress = estimateStressLevel()

        HealthSummary(
            heartRateAvg = heartRate,
            sleepHours = sleepHours,
            steps = steps,
            stressLevel = stress
        )
    }
}