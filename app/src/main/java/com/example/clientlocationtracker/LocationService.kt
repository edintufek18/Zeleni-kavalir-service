package com.example.clientlocationtracker

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.example.clientlocationtracker.Constants
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LocationService : Service() {
    var locationString = "xe"
    val db = Firebase.firestore
    // Define the location callback
    var locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if(locationResult != null && locationResult.lastLocation != null) {
                var latitude = locationResult.lastLocation!!.latitude
                var longitude = locationResult.lastLocation!!.longitude
                Log.d("LOCATION UPDATE", "Location: ${latitude}, ${longitude}")
                locationString  = "Location: ${latitude}, ${longitude}"
                db.collection("userLocation").
                document("8AUTudvJBi4xoQvr5M83").
                update("Location","${latitude},${longitude}")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
        throw UnsupportedOperationException("not implemented")
    }
    private fun startLocationService(){
        val channelId = "location_notification_channel"
        var notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)as NotificationManager
        var resultIntent = Intent()
        var pendingIntent = PendingIntent.getActivity(
            applicationContext,0,resultIntent,PendingIntent.FLAG_UPDATE_CURRENT
        )
        var builder = NotificationCompat.Builder(
            applicationContext,channelId
        )
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Location Service")
        builder.setDefaults(NotificationCompat.DEFAULT_ALL)
        builder.setContentText("Running")
        builder.setContentIntent(pendingIntent)
        builder.setAutoCancel(false)
        builder.setPriority(NotificationCompat.PRIORITY_MAX)

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(notificationManager != null
                && notificationManager.getNotificationChannel(channelId) == null){
                var notificationChannel = NotificationChannel(
                    channelId,"Location Service",NotificationManager.IMPORTANCE_HIGH
                )
                notificationChannel.description = "This channel is used by location service"
                notificationManager.createNotificationChannels(mutableListOf(notificationChannel))
            }
        }
        val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 4000)
            .setMinUpdateIntervalMillis(2000)
            .build()
//        var locationRequest = LocationRequest.create().apply {
//            interval = 4000
//            fastestInterval = 2000
//            priority = PRIORITY_HIGH_ACCURACY
//        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper())

    }
    private fun stopLocationService(){
        LocationServices.getFusedLocationProviderClient(this)
            .removeLocationUpdates(locationCallback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent != null){
            var action = intent.action
            if(action!= null){
                if(action.equals(Constants().ACTION_START_LOCATION_SERVICE)){
                    startLocationService()
                }else if(action.equals(Constants().ACTION_START_LOCATION_SERVICE)){
                    stopLocationService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

}