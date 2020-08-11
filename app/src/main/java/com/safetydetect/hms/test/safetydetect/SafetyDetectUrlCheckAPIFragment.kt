package com.safetydetect.hms.test.safetydetect

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.safetydetect.UrlCheckResponse
import com.huawei.hms.support.api.entity.safetydetect.UrlCheckThreat
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import com.safetydetect.hms.test.R
import kotlinx.android.synthetic.main.fg_urlcheck.*


class SafetyDetectUrlCheckAPIFragment : Fragment(), OnItemSelectedListener,
    View.OnClickListener {
    private var client: SafetyDetectClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = SafetyDetect.getClient(activity)
    }

   override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fg_urlcheck, container, false)
    }

    override fun onResume() {
        super.onResume()
        client!!.initUrlCheck()
    }

    override fun onPause() {
        super.onPause()
        client!!.shutdownUrlCheck()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fg_call_url_btn.setOnClickListener(this)
        val spinner = activity.findViewById<Spinner>(R.id.fg_url_spinner)
        spinner.onItemSelectedListener = this
        val adapter = ArrayAdapter.createFromResource(
            activity.applicationContext,
            R.array.url_array,
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    override fun onClick(view: View) {
        if (view.id === R.id.fg_call_url_btn) {
            callUrlCheckApi()
        }
    }

    override fun onItemSelected(
        adapterView: AdapterView<*>,
        view: View?,
        pos: Int,
        id: Long
    ) {
        val url = adapterView.getItemAtPosition(pos) as String
        val textView: EditText = activity.findViewById(R.id.fg_call_urlCheck_text)
        textView.setText(url)
        val testRes: EditText = activity.findViewById(R.id.fg_call_urlResult)
        testRes.setText("")
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}
    private fun callUrlCheckApi() {
        Log.i(TAG, "Start call URL check api")
        val editText: EditText = activity.findViewById(R.id.fg_call_urlCheck_text)
        val realUrl = editText.text.toString().trim { it <= ' ' }
        val testRes: EditText = activity.findViewById(R.id.fg_call_urlResult)
        client!!.urlCheck(
            realUrl,
            APP_ID.toString(),  // Specify url threat type
            UrlCheckThreat.MALWARE,
            UrlCheckThreat.PHISHING
        )
            .addOnSuccessListener { urlCheckResponse ->

                /**
                 * Called after successfully communicating with the SafetyDetect API.
                 * The #onSuccess callback receives an
                 * [UrlCheckResponse] that contains a
                 * list of UrlCheckThreat that contains the threat type of the Url.
                 */
                // Indicates communication with the service was successful.
                // Identify any detected threats.
                // Call getUrlCheckResponse method of UrlCheckResponse then you can get List<UrlCheckThreat> .
                // If List<UrlCheckThreat> is empty , that means no threats found , else that means threats found.
                val list =
                    urlCheckResponse.urlCheckResponse
                if (list.isEmpty()) {
                    // No threats found.
                    testRes.setText("No threats found.")
                } else {
                    // Threats found!
                    testRes.setText("Threats found!")
                }
            }
            .addOnFailureListener { e ->
                /**
                 * Called when an error occurred when communicating with the SafetyDetect API.
                 */
                // There was an error communicating with the service.
                val errorMsg: String? = if (e is ApiException) {
                    // An error with the Huawei Mobile Service API contains some
                    "Error: " +
                            SafetyDetectStatusCodes.getStatusCodeString(e.statusCode) + ": " +
                            e.message
                    // You can use the apiException.getStatusCode() method to get the status code.
                    // Note: If the status code is SafetyDetectStatusCodes.CHECK_WITHOUT_INIT, you need to call initUrlCheck().
                } else {
                    // Unknown type of error has occurred.
                    e.message
                }
                Log.d(TAG, errorMsg)
                Toast.makeText(
                    activity.applicationContext,
                    errorMsg,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        val TAG = SafetyDetectUrlCheckAPIFragment::class.java.simpleName
        private const val APP_ID = 102672053
    }
}