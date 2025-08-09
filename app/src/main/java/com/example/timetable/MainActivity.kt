package com.example.timetable

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.time.LocalTime

class MainActivity : ComponentActivity() {

    private val CHANNEL_ID = "timetable_notification_channel"
    private val NOTIFICATION_ID = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ask for POST_NOTIFICATIONS permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        createNotificationChannel()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = {
                        showNotification()
                    }) {
                        Text("Show Class Notification")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(onClick = {
                        cancelNotification()
                    }) {
                        Text("Turn Off Notification")
                    }
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Timetable Notifications"
            val descriptionText = "Shows current and next class info"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification() {
        val (nowClass, nextClass) = getClassInfo()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, don’t show notification
            return
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("📚 Current Period: $nowClass")
            .setContentText("Next: $nextClass")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }


    private fun cancelNotification() {
        with(NotificationManagerCompat.from(this)) {
            cancel(NOTIFICATION_ID)
        }
    }

    // Replace this logic with your real timetable
    private fun getClassInfo(): Pair<String, String> {
        val now = LocalTime.now()
        val day = java.time.LocalDate.now().dayOfWeek

        val timetable = mapOf(
            java.time.DayOfWeek.MONDAY to listOf("MPC-Lab", "MPC-Lab", "Advanced Metaverse - 202", "Advanced Metaverse - 202", "PDE", "3D Modelling - Lab", "3D Modelling - Lab"),
            java.time.DayOfWeek.TUESDAY to listOf("Free", "PDE", "Data Mining", "Cryptography", "MPC", "DAA Lab - 413", "DAA Lab - 413"),
            java.time.DayOfWeek.WEDNESDAY to listOf("PDE", "MPC", "3D Modelling - 208", "3D Modelling - 208", "Mentor", "Advanced Metaverse - 202", "Free"),
            java.time.DayOfWeek.THURSDAY to listOf("DAA", "Data Mining", "Data Mining Lab - 412", "Data Mining Lab - 412", "Social Media Marketing", "Social Media Marketing", "Social Media Marketing"),
            java.time.DayOfWeek.FRIDAY to listOf("MPC", "Data Mining", "Cryptography", "DAA", "PDE", "Cryptography", "DAA")
        )

        val periods = listOf(
            LocalTime.of(8, 30) to LocalTime.of(9, 30),
            LocalTime.of(9, 30) to LocalTime.of(10, 30),
            LocalTime.of(10, 45) to LocalTime.of(11, 40),
            LocalTime.of(11, 40) to LocalTime.of(12, 40),
            LocalTime.of(13, 40) to LocalTime.of(14, 40),
            LocalTime.of(14, 40) to LocalTime.of(15, 40),
            LocalTime.of(15, 40) to LocalTime.of(16, 40),
        )

        val subjectsToday = timetable[day] ?: listOf("Free", "Free", "Free", "Free", "Free", "Free", "Free")

        for (i in periods.indices) {
            val (start, end) = periods[i]
            if (now.isAfter(start) && now.isBefore(end)) {
                val nowClass = subjectsToday.getOrNull(i) ?: "Free"
                val nextClass = subjectsToday.getOrNull(i + 1) ?: "Done for Today"
                return Pair(nowClass, nextClass)
            }
        }

        return when {
            now.isBefore(periods.first().first) -> Pair("No Class Yet", subjectsToday.firstOrNull() ?: "None")
            now.isAfter(periods.last().second) -> Pair("College Over", "None")
            now.isAfter(LocalTime.of(12, 20)) && now.isBefore(LocalTime.of(13, 40)) -> Pair("Lunch Break", subjectsToday.getOrNull(4) ?: "Free")
            else -> Pair("Free Time", "Next Period Soon")
        }
    }


}
