package com.safetydetect.hms.test.safetydetect

import android.annotation.SuppressLint
import android.app.Fragment
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.api.entity.safetydetect.UserDetectResponse
import com.huawei.hms.support.api.safetydetect.SafetyDetect
import com.huawei.hms.support.api.safetydetect.SafetyDetectClient
import com.huawei.hms.support.api.safetydetect.SafetyDetectStatusCodes
import com.safetydetect.hms.test.R
import kotlinx.android.synthetic.main.fg_userdetect.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.ExecutionException


class SafetyDetectUserDetectAPIFragment : Fragment(), View.OnClickListener {
    private var client: SafetyDetectClient? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = SafetyDetect.getClient(activity)
    }

    override fun onResume() {
        super.onResume()
        client!!.initUserDetect()
    }

   override fun onPause() {
        super.onPause()
        client!!.shutdownUserDetect()
    }

   override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fg_userdetect, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fg_login_btn.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        if (v.getId() === R.id.fg_login_btn) {
            detect()
        }
    }

    private fun detect() {
        Log.i(TAG, "User detection start.")
        client!!.userDetection(APP_ID.toString())
            .addOnSuccessListener { userDetectResponse ->
                /**
                 * Called after successfully communicating with the SafetyDetect API.
                 * The #onSuccess callback receives an
                 * [UserDetectResponse] that contains a
                 * responseToken that can be used to get user detect result.
                 */
                // Indicates communication with the service was successful.
                Log.i(
                    TAG,
                    "User detection succeed, response = $userDetectResponse"
                )
                val verifySucceed =
                    verify(
                        userDetectResponse.responseToken
                    )
                if (verifySucceed) {
                    Toast.makeText(
                        activity.applicationContext,
                        "User detection succeed and verify succeed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                } else {
                    Toast.makeText(
                        activity.applicationContext,
                        "User detection succeed but verify fail, please replace verify url with your's server address",
                        Toast.LENGTH_SHORT
                    )
                        .show()
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
                Log.i(
                    TAG,
                    "User detection fail. Error info: $errorMsg"
                )
                Toast.makeText(
                    activity.applicationContext,
                    errorMsg,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        val TAG = SafetyDetectUserDetectAPIFragment::class.java.simpleName
        private const val APP_ID = 102672053

        /**
         * Send responseToken to your server to get the result of user detect.
         */
        private fun verify(responseToken: String): Boolean {
            return try {
                object : AsyncTask<String?, Void?, Boolean>() {
                    @SuppressLint("StaticFieldLeak")
                    override fun doInBackground(vararg params: String?): Boolean {
                        val input = params[0]
                        val jsonObject = JSONObject()
                        return try {
                            // TODO(developer): Replace the baseUrl with your own server address,better not hard code.
                            val baseUrl = "https://www.example.com/userdetect/verify"
                            jsonObject.put("response", input)
                            val result =
                                sendPost(
                                    baseUrl,
                                    jsonObject
                                )
                            val resultJson = JSONObject(result)
                            val success = resultJson.getBoolean("success")
                            // if success is true that means the user is real human instead of a robot.
                            Log.i(
                                TAG,
                                "verify: result = $success"
                            )
                            success
                        } catch (e: Exception) {
                            e.printStackTrace()
                            false
                        }
                    }
                }.execute(responseToken).get()
            } catch (e: ExecutionException) {
                e.printStackTrace()
                false
            } catch (e: InterruptedException) {
                e.printStackTrace()
                false
            }
        }

        /**
         * post the response token to yur own server.
         */
        @Throws(Exception::class)
        private fun sendPost(baseUrl: String, postDataParams: JSONObject): String? {
            val url = URL(baseUrl)
            val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
            conn.readTimeout = 20000
            conn.connectTimeout = 20000
            conn.requestMethod = "POST"
            conn.doInput = true
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json")
            conn.outputStream.use { os ->
                BufferedWriter(OutputStreamWriter(os, StandardCharsets.UTF_8)).use { writer ->
                    writer.write(postDataParams.toString())
                    writer.flush()
                }
            }
            val responseCode: Int = conn.responseCode // To Check for 200
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val `in` = BufferedReader(InputStreamReader(conn.inputStream))
                val sb = StringBuffer()
                var line: String?
                while (`in`.readLine().also { line = it } != null) {
                    sb.append(line)
                    break
                }
                `in`.close()
                return sb.toString()
            }
            return null
        }
    }
}