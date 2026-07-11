package com.linglonghu

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException

class WebAppInterface(private val context: Context) {

    @android.webkit.JavascriptInterface
    fun getPlatform(): String {
        return "Android"
    }

    @android.webkit.JavascriptInterface
    fun getSystemName(): String {
        return "Android"
    }

    @android.webkit.JavascriptInterface
    fun alert(message: String) {
        android.app.AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    @android.webkit.JavascriptInterface
    fun log(message: String) {
        Log.d("WebApp", message)
    }

    @android.webkit.JavascriptInterface
    fun openLink(url: String) {
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
        intent.data = android.net.Uri.parse(url)
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @android.webkit.JavascriptInterface
    fun getTimezone(): String {
        return java.util.TimeZone.getDefault().id
    }

    @android.webkit.JavascriptInterface
    fun getVersion(): String {
        return "3.6.3"
    }
}
