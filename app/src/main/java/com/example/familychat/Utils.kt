package com.example.familychat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class Utils {
    companion object{
        fun formatTimestamp(time: Long): String {
            val currentDate = Calendar.getInstance()
            val messageDate = Calendar.getInstance().apply {
                timeInMillis = time
            }

            val dateFormat: SimpleDateFormat

            if (currentDate.get(Calendar.YEAR) == messageDate.get(Calendar.YEAR) &&
                currentDate.get(Calendar.DAY_OF_YEAR) == messageDate.get(Calendar.DAY_OF_YEAR)
            ) {
                dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            } else {
                dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
            }
            return dateFormat.format(messageDate.time)
        }
        val CHANNEL_ID = Random.nextInt().toString()
        val CHANNEL_NAME = "FamilyChat"
        val CHANNEL_DESCRIPTION = "Description"

    }

}