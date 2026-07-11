package com.linglonghu

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.OkHttpClient
import okhttp3.Request

object UpdateChecker {

    interface OnUpdateListener {
        fun onUpdateAvailable(version: String, changelog: String, apkUrl: String)
        fun onNoUpdate()
    }

    private const val VERSION_URL = "https://rider-update.linglonghu.com/version.json"

    fun check(context: Context, currentVersion: String, listener: OnUpdateListener) {
        Thread {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .build()

                val request = Request.Builder().url(VERSION_URL).build()
                val response = client.newCall(request).execute()

                if (response.isSuccessful) {
                    val json = response.body?.string()
                    val gson = Gson()
                    val versionInfo = gson.fromJson(json, JsonObject::class.java)

                    val latestVersion = versionInfo.get("version")?.asString ?: ""
                    val platforms = versionInfo.getAsJsonObject("platforms")

                    if (platforms != null && platforms.has("android")) {
                        val androidPlatform = platforms.getAsJsonObject("android")
                        val apkKey = androidPlatform.get("exe_key")?.asString ?: ""
                        val changelog = versionInfo.get("changelog")?.asString ?: ""

                        if (compareVersions(latestVersion, currentVersion) > 0) {
                            val apkUrl = VERSION_URL.replace("version.json", apkKey)
                            listener.onUpdateAvailable(latestVersion, changelog, apkUrl)
                            return@Thread
                        }
                    }
                }

                listener.onNoUpdate()

            } catch (e: Exception) {
                Log.e("UpdateChecker", "Check update failed", e)
                listener.onNoUpdate()
            }
        }.start()
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".")
        val parts2 = v2.split(".")
        val minLen = Math.min(parts1.size, parts2.size)

        for (i in 0 until minLen) {
            try {
                val num1 = parts1[i].toInt()
                val num2 = parts2[i].toInt()
                if (num1 != num2) {
                    return num1 - num2
                }
            } catch (e: NumberFormatException) {
                return v1.compareTo(v2)
            }
        }

        return parts1.size - parts2.size
    }
}
