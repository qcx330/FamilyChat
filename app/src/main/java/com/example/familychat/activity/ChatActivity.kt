package com.example.familychat.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.familychat.adapter.MessageAdapter
import com.example.familychat.adapter.RvInterface
import com.example.familychat.databinding.ActivityChatBinding
import com.example.familychat.model.MessageType
import com.example.familychat.model.User
import com.example.familychat.viewmodel.ChatViewModel
import com.example.familychat.viewmodel.MessageViewModel
import com.example.familychat.viewmodel.UserViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.util.Locale

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var messChat: RecyclerView
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var messageViewModel: MessageViewModel
    lateinit var currentChat: String
    lateinit var currentFamily: String
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionCode = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        chatViewModel = ViewModelProvider(this).get(ChatViewModel::class.java)
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)
        val chatId = intent.getStringExtra("id")!!
        currentChat = chatId

        messChat = binding.messageChat
        messChat.layoutManager = LinearLayoutManager(this)
        val layoutManager = messChat.layoutManager as LinearLayoutManager

        val adapter = MessageAdapter(object : RvInterface {
            override fun OnClickItem(pos: Int) {
                if (currentChat == currentFamily) {
                    messageViewModel.retrieveFamilyMessage(currentChat)
                    messageViewModel.getMessageList().observe(this@ChatActivity) {
                        if (it.isNotEmpty()) {
                            if (it[pos].type == MessageType.LOCATION) {
                                val (latitude, longitude, detail) = it[pos].content!!.split(",")
                                val latitudeDouble = latitude.toDouble()
                                val longitudeDouble = longitude.toDouble()
                                val mapUri =
                                    Uri.parse("https://maps.google.com/maps/search/$latitudeDouble,$longitudeDouble")
                                val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                startActivity(intent)
                            }
                        }
                    }
                } else {
                    messageViewModel.retrieveUserMessage(currentChat)
                    messageViewModel.getMessageList().observe(this@ChatActivity) {
                        if (it.isNotEmpty()) {
                            if (it[pos].type == MessageType.LOCATION) {
                                val (latitude, longitude, detail) = it[pos].content!!.split(",")
                                val latitudeDouble = latitude.toDouble()
                                val longitudeDouble = longitude.toDouble()
                                val mapUri =
                                    Uri.parse("https://maps.google.com/maps/search/$latitudeDouble,$longitudeDouble")
                                val intent = Intent(Intent.ACTION_VIEW, mapUri)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }

            override fun OnRemoveItem(pos: Int) {

            }
        })

        messChat.adapter = adapter

        userViewModel.getCurrentFamily()
        userViewModel.getCurrentFamilyId().observe(this) { familyId ->
            if (familyId != null) {
                currentFamily = familyId
                if (familyId == chatId) {
                    chatViewModel.retrieveFamilyChat(familyId)
                    chatViewModel.getChatRoom().observe(this) {
                        if (it != null) {
                            binding.tvName.text = it.roomName
                            Log.d("chatroomName", it.roomName!!)
                        }
                    }
                    binding.tvName.text = "Family"
                    messageViewModel.retrieveFamilyMessage(chatId)
                    messageViewModel.getMessageList().observe(this) { list ->
                        if (list != null) {

                            val sortedList = list.sortedBy { it.time }
                            adapter.updateList(sortedList)
                            val lastVisibleItemPosition =
                                layoutManager.findLastVisibleItemPosition()
                            if (lastVisibleItemPosition != RecyclerView.NO_POSITION) {
                                messChat.smoothScrollToPosition(lastVisibleItemPosition)
                            } else {
                                messChat.smoothScrollToPosition(adapter.itemCount - 1)
                            }
//                            messChat.post {
//                                val lastItemPosition = adapter.itemCount - 1
//                                messChat.smoothScrollToPosition(lastItemPosition)
//                            }
                        } else Log.d("Chat list", "null")
                    }
                } else {
                    chatViewModel.getChatRoom(chatId)
                    chatViewModel.getChatRoom().observe(this) {
                        if (it != null) {
                            val userId =
                                it.member!!.firstOrNull { it != auth.currentUser!!.uid }
                            Log.d("chatroomid", it.roomId!!)
                            Log.d("userId", userId!!)
                            Log.d("user", userViewModel.fetchDataUserById(userId).toString())
                            userViewModel.fetchDataUserById(userId)
                            userViewModel.getUser().observe(this) { user ->
                                user?.let { binding.tvName.text = user.name }
                            }

                        } else Log.d("chatroomid", "null")
                    }
                    messageViewModel.retrieveUserMessage(chatId)
                    messageViewModel.getMessageList().observe(this) { list ->
                        if (list != null) {
                            val sortedList = list.sortedBy { it.time }
                            adapter.updateList(sortedList)
                            val lastVisibleItemPosition =
                                layoutManager.findLastVisibleItemPosition()
                            if (lastVisibleItemPosition != RecyclerView.NO_POSITION) {
                                messChat.smoothScrollToPosition(lastVisibleItemPosition)
                            } else {
                                messChat.smoothScrollToPosition(adapter.itemCount)
                            }
                        } else Log.d("Chat list", "null")
                    }
                }

            } else Log.d("getFamilyIdChat", "null")
        }

        binding.btnBack.setOnClickListener() {
            finish()
        }
        binding.btnSend.setOnClickListener() {
            val text = binding.edtText.text.toString()
            if (currentFamily == chatId) {
                messageViewModel.sendFamilyMessage(text, chatId)
                messChat.post {
                    val lastItemPosition = adapter.itemCount - 1
                    messChat.smoothScrollToPosition(lastItemPosition)
                }
                userViewModel.getUsersInFamily(chatId)
                userViewModel.getUserList().observe(this) { userList ->
                    if (userList.isNotEmpty()) {
                        sendNotification(text, userList, chatId)
                    }
                }
            } else {
                messageViewModel.sendUserMessage(text, chatId)
                messChat.post {
                    val lastItemPosition = adapter.itemCount - 1
                    messChat.smoothScrollToPosition(lastItemPosition)
                }
                chatViewModel.retrieveMemberList(chatId)
                chatViewModel.getMemberList().observe(this) { userList ->
                    if (userList.isNotEmpty()) {
                        sendNotification(text, userList, chatId)
                    }
                }
            }
            binding.edtText.text.clear()
            adapter.notifyDataSetChanged()

        }
        binding.btnAttach.setOnClickListener() {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
            adapter.notifyDataSetChanged()
        }
        binding.btnShareLocation.setOnClickListener() {
            if (hasLocationPermissions()) {
                requestLocationUpdates()
            } else {
                requestLocationPermission()
            }
            adapter.notifyDataSetChanged()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let {
                if (currentFamily == currentChat) {
                    messageViewModel.sendImageFamilyChat(it, currentFamily)
                    userViewModel.getUserList().observe(this) { userList ->
                        if (userList.isNotEmpty()) {
                            sendNotification("sent an image", userList, currentFamily)
                        }
                    }

                } else {
                    messageViewModel.sendImageUserChat(it, currentChat)
                    chatViewModel.getMemberList().observe(this) { userList ->
                        if (userList.isNotEmpty()) {
                            sendNotification("sent an image", userList, currentChat)
                        }
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun sendNotification(message: String, userList: List<User>, chatId: String) {
        userViewModel.getCurrentUser().observe(this) { currentUser ->
            for (user in userList.filter { it.id != currentUser.id }) {
                try {
                    val jsonObject = JSONObject()
                    val notificationObj = JSONObject()
                    if (chatId == currentFamily) {
                        notificationObj.put("title", "Family")
                        notificationObj.put("body", "${currentUser.name}: $message")
                    } else {
                        notificationObj.put("title", currentUser.name)
                        notificationObj.put("body", message)
                    }
                    val dataObj = JSONObject()
                    dataObj.put("chatId", chatId)
                    jsonObject.put("notification", notificationObj)
                    jsonObject.put("data", dataObj)
                    jsonObject.put("to", user.token)
                    Log.d("send notification to", user.name)
                    callApi(jsonObject)
                } catch (e: Exception) {
                    Log.e("send notification", e.message.toString())
                }
            }
        }
    }

    fun callApi(jsonObject: JSONObject) {
        val json = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"
        val headers = Headers.headersOf(
            "Authorization",
            "Bearer AAAAk3sOXv0:APA91bF5JwkYRRVKPg-uwuBayOT9MuodeDWzZlnGECxYZs7913bEfe6vwB43FYxALmd5ZUc4udJme4zxB3JeO2juK59QRtNhHWsWCNJsoB3eQbt_4YuVSY8lp1YqDyizFZDwqoHRgKox"
        )

        val body = RequestBody.create(json, jsonObject.toString())

        val request = Request.Builder().apply {
            url(url)
            method("POST", body)
            headers(headers)
        }.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("call back send noti", e.message.toString())
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    Log.d("FCM Response", responseBody ?: "Empty response body")
                } else {
                    Log.e("FCM Request Failed", response.body?.string() ?: "Empty response body")
                }
            }

        })
    }

    private fun hasLocationPermissions(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            locationPermissionCode
        )
    }

    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())

                        try {
                            val addresses =
                                geocoder.getFromLocation(location.latitude, location.longitude, 1)

                            if (addresses!!.isNotEmpty()) {
                                val address = addresses[0]

                                val city = address.locality
                                val state = address.adminArea
                                val country = address.countryName
                                val locationDetails = "$city, $state, $country"
                                val currentLocation =
                                    "${location.latitude},${location.longitude},${locationDetails}"
                                if (currentChat == currentFamily) {
                                    messageViewModel.sendLocation(
                                        currentLocation,
                                        currentChat,
                                        "FamilyChat"
                                    )
                                    userViewModel.getUserList().observe(this) { userList ->
                                        if (userList.isNotEmpty()) {
                                            sendNotification(
                                                "sent a location",
                                                userList,
                                                currentFamily
                                            )
                                        }
                                    }
                                } else {
                                    messageViewModel.sendLocation(
                                        currentLocation,
                                        currentChat,
                                        "UserChat"
                                    )
                                    chatViewModel.getMemberList().observe(this) { userList ->
                                        if (userList.isNotEmpty()) {
                                            sendNotification(
                                                "sent a location",
                                                userList,
                                                currentChat
                                            )
                                        }
                                    }
                                }
                            }
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Location not available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}