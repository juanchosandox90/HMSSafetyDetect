package com.safetydetect.hms.test.safetydetect

import android.app.Fragment
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import com.safetydetect.hms.test.R
import kotlinx.android.synthetic.main.fg_sysintegrity.*
import org.json.JSONException
import org.json.JSONObject


class SafetyDetectSysIntegrityAPIFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fg_sysintegrity, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fg_button_sys_integrity_go.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.id == R.id.fg_button_sys_integrity_go) {
            processView()
            invokeSysIntegrity()
        }
    }

    private fun invokeSysIntegrity() {
        // TODO(developer): Change the nonce generation to include your own value.
        val nonce =
            ("Sample" + System.currentTimeMillis()).toByteArray()
        SafetyDetect.getClient(activity)
            .sysIntegrity(nonce,
                APP_ID
            )
            .addOnSuccessListener { response -> // Indicates communication with the service was successful.
                // Use response.getResult() to get the result data.
                val jwsStr = response.result
                // Process the result data here
                val jwsSplit =
                    jwsStr.split("\\.".toRegex()).toTypedArray()
                val jwsPayloadStr = jwsSplit[1]
                val payloadDetail =
                    String(Base64.decode(jwsPayloadStr.toByteArray(), Base64.URL_SAFE))
                try {
                    val jsonObject = JSONObject(payloadDetail)
                    val basicIntegrity: Boolean = jsonObject.getBoolean("basicIntegrity")
                    fg_button_sys_integrity_go.setBackgroundResource(if (basicIntegrity) R.drawable.btn_round_green else R.drawable.btn_round_red)
                    fg_button_sys_integrity_go.setText(R.string.rerun)
                    val isBasicIntegrity = basicIntegrity.toString()
                    val basicIntegrityResult =
                        "Basic Integrity: $isBasicIntegrity"
                    fg_payloadBasicIntegrity!!.text = basicIntegrityResult
                    if (!basicIntegrity) {
                        val advice = "Advice: " + jsonObject.getString("advice")
                        fg_payloadAdvice!!.text = advice
                    }
                } catch (e: JSONException) {
                    val errorMsg = e.message
                    Log.e(
                        TAG,
                        errorMsg ?: "unknown error"
                    )
                }
            }
            .addOnFailureListener { e -> // There was an error communicating with the service.
                val errorMsg: String? = if (e is ApiException) {
                    // An error with the HMS API contains some additional details.
                    SafetyDetectStatusCodes.getStatusCodeString(e.statusCode) +
                            ": " + e.message
                    // You can use the apiException.getStatusCode() method to get the status code.
                } else {
                    // Unknown type of error has occurred.
                    e.message
                }
                Log.e(TAG, errorMsg!!)
                Toast.makeText(
                    activity.applicationContext,
                    errorMsg,
                    Toast.LENGTH_SHORT
                ).show()
                fg_button_sys_integrity_go.setBackgroundResource(R.drawable.btn_round_yellow)
                fg_button_sys_integrity_go.setText(R.string.rerun)
            }
    }

    private fun processView() {
        fg_payloadBasicIntegrity!!.text = ""
        fg_payloadAdvice!!.text = ""
        fg_textView_title.text = ""
        fg_button_sys_integrity_go!!.setText(R.string.processing)
        fg_button_sys_integrity_go!!.setBackgroundResource(R.drawable.btn_round_procesing)
    }

    companion object {
        val TAG = SafetyDetectSysIntegrityAPIFragment::class.java.simpleName
        private const val APP_ID = "102672053"
    }
}