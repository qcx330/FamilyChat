package com.example.familychat.notification

import com.example.familychat.model.PushNotification
import com.example.familychat.notification.Constants.Companion.CONTENT_TYPE
import com.example.familychat.notification.Constants.Companion.SERVER_KEY
import retrofit2.Response
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {
    @Headers("Authorization: key=$SERVER_KEY","Content-type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification: PushNotification
    ): Response<ResponseBody>
}