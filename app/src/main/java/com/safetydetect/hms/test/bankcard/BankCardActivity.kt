package com.safetydetect.hms.test.bankcard

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.huawei.hms.mlplugin.card.bcr.MLBcrCapture
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureConfig
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureFactory
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureResult
import com.safetydetect.hms.test.R
import kotlinx.android.synthetic.main.activity_bank_card.*
import java.lang.StringBuilder

class BankCardActivity : AppCompatActivity(), View.OnClickListener {

    private val CAMERA_PERMISSION_CODE = 1
    private val READ_EXTERNAL_STORAGE_CODE = 2
    private var cardResultFront = ""

    private var mTextView: TextView? = null
    private var previewImage: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_bank_card)

        mTextView = findViewById(R.id.text_result)
        previewImage = findViewById(R.id.Bank_Card_image)

        previewImage!!.setScaleType(ImageView.ScaleType.FIT_XY)

        detect.setOnClickListener(this)

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            this.requestCameraPermission()
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            this.requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        val permission = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            ActivityCompat.requestPermissions(this, permission, CAMERA_PERMISSION_CODE)
        }
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            ActivityCompat.requestPermissions(this, permission, READ_EXTERNAL_STORAGE_CODE)
        }
    }

    private fun formatIdCardResult(bankCardResult: MLBcrCaptureResult): String? {
        val resultBuilder = StringBuilder()
        resultBuilder.append("Number: ")
        resultBuilder.append(bankCardResult.expire)
        resultBuilder.append("\r\n")
        Log.i("BankCardActivity: ", "Resultado: $resultBuilder")
        return resultBuilder.toString()
    }

    private fun displayFailure() {
        mTextView!!.text = "Failure"
    }

    private val bankCallback: MLBcrCapture.Callback = object : MLBcrCapture.Callback {
        override fun onSuccess(bankCardResult: MLBcrCaptureResult) {
            Log.i("BankCardActivity: ", "Callback onSuccess")
            // Validacion del algoritmo del tipo de tarjeta.
            Log.i("BankCardActivity:", bankCardResult.expire)
            val bitmap = bankCardResult.originalBitmap
            this@BankCardActivity.previewImage!!.setImageBitmap(bitmap)
            this@BankCardActivity.cardResultFront =
                this@BankCardActivity.formatIdCardResult(bankCardResult)!!
            this@BankCardActivity.mTextView!!.text = this@BankCardActivity.cardResultFront
        }

        override fun onFailure(p0: Int, p1: Bitmap?) {
            this@BankCardActivity.displayFailure()
            Log.i("BankCardActivity: ", "CallbackFailureCapture")
        }

        override fun onCanceled() {
            Log.i("BankCardActivity: ", "UserCancelledCapture")
        }

        override fun onDenied() {
            this@BankCardActivity.displayFailure()
            Log.i("BankCardActivity: ", "CallbackBankCardCameraDenied")
        }

    }

    private fun startCaptureActivity(Callback: MLBcrCapture.Callback) {
        val config =
            MLBcrCaptureConfig.Factory()
                .setOrientation(MLBcrCaptureConfig.ORIENTATION_AUTO)
                .create()
        val bcrCapture = MLBcrCaptureFactory.getInstance().getBcrCapture(config)
        bcrCapture.captureFrame(this, Callback)
    }

    override fun onClick(v: View?) {
        this.mTextView!!.text = ""
        this.startCaptureActivity(this.bankCallback)
    }
}