package com.zenia.app.data

import android.content.Context
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.zenia.app.di.DefaultDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.Instant
import javax.inject.Inject

class HealthConnectRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher
) {
    private lateinit var healthConnectClient: HealthConnectClient

    var isClientAvailable: Boolean = false
        private set

    init {
        val availability = HealthConnectClient.getSdkStatus(context)

        if (availability == HealthConnectClient.SDK_AVAILABLE) {
            healthConnectClient = HealthConnectClient.getOrCreate(context)
            isClientAvailable = true
        } else {
            isClientAvailable = false
        }
    }

    fun getAvailabilityStatus(): Int {
        return HealthConnectClient.getSdkStatus(context)
    }

    val permissions: Set<String> = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class)
    )

    fun getPermissionRequestContract(): ActivityResultContract<Set<String>, Set<String>> {
        return androidx.health.connect.client.PermissionController.createRequestPermissionResultContract()
    }

    suspend fun hasPermissions(): Boolean {
        if (!isClientAvailable) return false

        val granted = healthConnectClient.permissionController.getGrantedPermissions()
        return granted.containsAll(permissions)
    }

    private suspend fun getHeartRateRecordsForLastDay(): List<HeartRateRecord> {
        if (!isClientAvailable) return emptyList()

        return try {
            val endTime = Instant.now()
            val startTime = endTime.minus(1, java.time.temporal.ChronoUnit.DAYS)

            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            response.records
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            emptyList()
        }
    }

    suspend fun readDailyHeartRate(): List<HeartRateRecord> {
        return getHeartRateRecordsForLastDay()
    }

    suspend fun readDailyHeartRateAverage(): Int? = withContext(defaultDispatcher) {
        val records = getHeartRateRecordsForLastDay()

        if (records.isEmpty()) return@withContext null

        val allSamplesBpm = records.flatMap { record ->
            record.samples.map { sample -> sample.beatsPerMinute }
        }

        if (allSamplesBpm.isEmpty()) return@withContext null

        return@withContext allSamplesBpm.average().toInt()
    }
}