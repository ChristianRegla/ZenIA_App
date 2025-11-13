package com.zenia.app.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import java.time.ZonedDateTime

@RequiresApi(Build.VERSION_CODES.P)
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
}