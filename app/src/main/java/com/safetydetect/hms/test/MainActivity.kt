package com.safetydetect.hms.test

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.safetydetect.hms.test.bankcard.BankCardActivity
import com.safetydetect.hms.test.location.LocationActivity
import com.safetydetect.hms.test.safetydetect.SafetyActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}