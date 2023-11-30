package com.example.familychat.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.familychat.activity.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class FirebaseService:FirebaseMessagingService() {
//    private val CHANNEL_ID = "my_notification_channel"
//    companion object{
//        var sharedPref:SharedPreferences? = null
//        var token:String?
//            get(){
//                return sharedPref?.getString("token","")
//            }
//            set(value) {
//                sharedPref?.edit()?.putString("token", value)?.apply()
//            }
//    }
//
//    override fun onNewToken(newToken: String) {
//        super.onNewToken(newToken)
//        token = newToken
//    }
//
    override fun onMessageReceived(message: RemoteMessage) {
//        super.onMessageReceived(message)
//        val intent = Intent(this, MainActivity::class.java)
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        val notificationId = Random.nextInt()

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            createNotificationChannel(notificationManager)
//        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//        val pendingIntent = PendingIntent.getActivity(this,0,intent,
//            PendingIntent.FLAG_MUTABLE)
//        val notification = NotificationCompat.Builder(this,CHANNEL_ID)
//            .setContentTitle(message.data["title"])
//            .setContentText(message.data["message"])
//            .setSmallIcon(R.drawable.logo)
//            .setAutoCancel(true)
//            .setContentIntent(pendingIntent)
//            .build()
//
//        notificationManager.notify(notificationId,notification)
    }
//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun createNotificationChannel(notificationManager: NotificationManager){
//
//        val channelName = "ChannelFirebaseChat"
//        val channel = NotificationChannel(CHANNEL_ID,channelName,IMPORTANCE_HIGH).apply {
//            description="MY FIREBASE CHAT DESCRIPTION"
//            enableLights(true)
//            lightColor = Color.WHITE
//        }
//        notificationManager.createNotificationChannel(channel)
//    }
}