package com.safetydetect.hms.test.safetydetect


import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import com.safetydetect.hms.test.R
import kotlinx.android.synthetic.main.fg_wifidetect.*


class SafetyDetectWifiDetectAPIFragment : Fragment(), View.OnClickListener {
   override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fg_wifidetect, container, false)
    }

   override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
       fg_get_wifidetect_status.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        val id: Int = v.id
        if (id == R.id.fg_get_wifidetect_status) {
            wifiDetectStatus
        }
    }// Unknown type of error has occurred.// An error with the HMS API contains some additional details.
    // You can use the apiException.getStatusCode() method to get the status code.
// There was an error communicating with the service.

    /**
     * getWifiDetectStatus()  Get Wifi Status.
     */
    private val wifiDetectStatus: Unit
        get() {
            SafetyDetect.getClient(activity)
                .wifiDetectStatus
                .addOnSuccessListener { wifiDetectResponse ->
                    val wifiDetectStatus = wifiDetectResponse.wifiDetectStatus
                    val wifiDetectView = "WifiDetect status: $wifiDetectStatus"
                    fg_wifidetecttextView!!.text = wifiDetectView
                }
                .addOnFailureListener { e -> // There was an error communicating with the service.
                    val errorMsg: String? = if (e is ApiException) {
                    // An error with the HMS API contains some additional details.
                    (SafetyDetectStatusCodes.getStatusCodeString(e.statusCode) + ": "
                            + e.message)
                    // You can use the apiException.getStatusCode() method to get the status code.
                } else {
                    // Unknown type of error has occurred.
                    e.message
                }
                    val msg = "Get wifiDetect status failed! Message: $errorMsg"
                    Log.e(TAG, msg)
                    fg_wifidetecttextView!!.text = msg
                }
        }

    companion object {
        val TAG = SafetyDetectWifiDetectAPIFragment::class.java.simpleName
    }
}