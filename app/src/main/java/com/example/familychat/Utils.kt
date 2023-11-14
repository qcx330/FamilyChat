package com.example.familychat

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

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
                dateFormat = SimpleDateFormat("hh:mm", Locale.getDefault())
            } else {
                dateFormat = SimpleDateFormat("hh:mm DD/MM/yyyy", Locale.getDefault())
            }
            return dateFormat.format(messageDate.time)
        }
    }

}