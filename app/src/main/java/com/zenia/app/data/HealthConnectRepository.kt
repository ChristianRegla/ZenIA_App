package com.zenia.app.data

import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.RequiresApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime

class HealthConnectRepository(private val context: Context) {
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

    suspend fun readDailyHeartRate(): List<HeartRateRecord> {
        if (!isClientAvailable) return emptyList()

        val startTime = ZonedDateTime.now().minusDays(1).toInstant()
        val endTime = Instant.now()

        try {
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)
            return response.records
        } catch (e: Exception) {
            return emptyList()
        }
    }

    suspend fun readDailyHeartRateAverage(): Int? {
        if (!isClientAvailable || !hasPermissions()) return null
        val startTime = ZonedDateTime.now().minusDays(1).toInstant()
        val endTime = Instant.now()

        try {
            val request = ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
            val response = healthConnectClient.readRecords(request)

            val allSamplesBpm = response.records.flatMap { record ->
                record.samples.map { sample -> sample.beatsPerMinute }
            }

            if (allSamplesBpm.isEmpty()) return null

            return allSamplesBpm.average().toInt()
        } catch (e: Exception) {
            return null
        }
    }
}