package com.safetydetect.hms.test

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.safetydetect.hms.test.bankcard.BankCardActivity
import com.safetydetect.hms.test.location.LocationActivity
import com.safetydetect.hms.test.safetydetect.SafetyActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var locationManager: LocationManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //gpsEnabled()
        btnSafetyDetect.setOnClickListener {
            startActivity(Intent(this@MainActivity, SafetyActivity::class.java))
        }

        btnBankCard.setOnClickListener {
            startActivity(Intent(this@MainActivity, BankCardActivity::class.java))
        }

        btnLocation.setOnClickListener {
            startActivity(Intent(this@MainActivity, LocationActivity::class.java))
        }
    }

    private fun gpsEnabled() {
        if (!locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setMessage("¿Deseas Activar tu GPS?")
                .setCancelable(true)
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }
                .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
            val alert: AlertDialog = builder.create()
            alert.show()
        } else {
            Toast.makeText(this, "GPS Activado", Toast.LENGTH_LONG).show()
        }
    }
}