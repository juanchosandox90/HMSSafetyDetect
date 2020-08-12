package com.safetydetect.hms.test.location

import android.content.Context
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.huawei.hms.common.ApiException
import com.huawei.hms.common.ResolvableApiException
import com.huawei.hms.location.*
import com.safetydetect.hms.test.R
import kotlinx.android.synthetic.main.activity_location.*

class LocationActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_LOCATION = 9000
    private var mLocationRequest: LocationRequest? = null
    private var settingsClient: SettingsClient? = null
    private var locationManager: LocationManager? = null

    private val fusedLocation: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //Validar los persmisos
        if (checkPermission()) {
            initSettings()
        }
    }

    private fun initSettings() {
        //Cuando este iniciados los settings vamos a llamar la funcion para pedir localizacion con callback
        settingsClient = LocationServices.getSettingsClient(this)
        mLocationRequest = LocationRequest().apply {
            interval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        //Llamar funcion que recibe el callback.
        requestLocationUpdatesWithCallback()
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val permissions = arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_LOCATION)
                return false
            }
        }
        return true
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val locations = locationResult.locations
            if (locations.isNotEmpty()) {
                val location = locations[0]
                tv_location_actual?.text =
                    "LAT: ${location.latitude} <--> LONG: ${location.longitude}"
            }
        }

        override fun onLocationAvailability(locationAvailability: LocationAvailability) {
            if (!locationAvailability.isLocationAvailable) {
                Toast.makeText(
                    applicationContext, "Check GPS", Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // Funcion que llama el callback para actualizar la localizacion
    private fun requestLocationUpdatesWithCallback() {
        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        val locationSettingsRequest = builder.build()
        settingsClient?.checkLocationSettings(locationSettingsRequest)?.addOnSuccessListener {
            Log.i("LocationActivity", "Location Settings Success")
            fusedLocation.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper()
            )?.addOnSuccessListener {
                Log.i("LocationActivity: ", "Request Location Update Success")
            }?.addOnFailureListener { e ->
                Log.e("LocationActivity: ", "Request Location Update Failed: ${e.message}")
            }
        }?.addOnFailureListener { e ->
            Log.e("LocationActivity: ", "Location Settings Failed: ${e.message}")
            when ((e as ApiException).statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    val resolvableApiException = e as ResolvableApiException
                    resolvableApiException.startResolutionForResult(this, 0)
                } catch (sie: IntentSender.SendIntentException) {
                    Log.e("LocationActivity: ", "Err ${sie.message}")
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
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.size > 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                initSettings()
            } else {
                Toast.makeText(this, "Permissions Denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun removeLocationUpdatedWithCallback() {
        fusedLocation.removeLocationUpdates(mLocationCallback)
    }

    override fun onDestroy() {
        removeLocationUpdatedWithCallback()
        super.onDestroy()
    }
}