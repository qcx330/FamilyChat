package com.example.familychat

import java.util.concurrent.TimeUnit

class Utils {
    companion object{
        fun getTimeAgo(time: Long): String {
            val currentTimeMillis = System.currentTimeMillis()
            val timeDifference = currentTimeMillis - time

            return when {
                timeDifference < TimeUnit.SECONDS.toMillis(1) -> "just now"
                timeDifference < TimeUnit.MINUTES.toMillis(1) -> {
                    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDifference)
                    "$seconds seconds ago"
                }
                timeDifference < TimeUnit.HOURS.toMillis(1) -> {
                    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDifference)
                    "$minutes minutes ago"
                }
                timeDifference < TimeUnit.DAYS.toMillis(1) -> {
                    val hours = TimeUnit.MILLISECONDS.toHours(timeDifference)
                    "$hours hours ago"
                }
                else -> {
                    val days = TimeUnit.MILLISECONDS.toDays(timeDifference)
                    "$days days ago"
                }
            }
        }
    }

}