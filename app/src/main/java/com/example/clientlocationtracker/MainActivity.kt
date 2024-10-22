package com.example.clientlocationtracker

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clientlocationtracker.databinding.ActivityMainBinding
import com.example.clientlocationtracker.LocationService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


private lateinit var startServiceButton: Button
private lateinit var stopServiceButton: Button
private lateinit var locationTextView: TextView

private const val REQUEST_CODE_LOCATION_PERMISSION = 1
class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val db = Firebase.firestore
        locationTextView = findViewById(R.id.coordinateText)

        startServiceButton = findViewById(R.id.startUpdateLocationButton)
        startServiceButton.setOnClickListener{
            if(ContextCompat.checkSelfPermission(
                applicationContext,android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(
                    this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_CODE_LOCATION_PERMISSION
                )
            } else {
                startLocationService()
                db.collection("userLocation")
                    .addSnapshotListener { value, e ->
                        if (e != null) {
                            return@addSnapshotListener
                        }

                        for (doc in value!!) {

                            locationTextView.text = doc.getString("Location")?.toString()
                        }

                    }

                locationTextView.text = LocationService().locationString
            }
        }


        stopServiceButton = findViewById(R.id.stopUpdateLocationButton)
        stopServiceButton.setOnClickListener{
            stopLocationService()
            locationTextView.text = "---"
        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE_LOCATION_PERMISSION
            && grantResults.size > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startLocationService()
            }else{
                Toast.makeText(this,"Permission denied",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isLocationServiceRunning():Boolean{
        var activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        if(activityManager != null){
            for ( service in activityManager.getRunningServices(Int.MAX_VALUE)){
                if(service.service.className.equals(LocationService::class.qualifiedName)){
                    if(service.foreground){
                        return true
                    }
                }
            }
            return false
        }
        return false
    }
    private fun startLocationService(){
        var intent = Intent(applicationContext,LocationService::class.java)
        intent.setAction(Constants().ACTION_START_LOCATION_SERVICE)
        startService(intent)
        Toast.makeText(this,"Location service started",Toast.LENGTH_LONG).show()
    }

    private fun stopLocationService(){
        var intent = Intent(applicationContext,LocationService::class.java)
        intent.setAction(Constants().ACTION_STOP_LOCATION_SERVICE)
        startService(intent)
        Toast.makeText(this,"Location service stopped",Toast.LENGTH_LONG).show()
    }
}
