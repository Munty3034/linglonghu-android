package com.linglonghu

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private val BASE_URL = "https://rider.linglonghu.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = WebView(this)
        refreshLayout = SwipeRefreshLayout(this)

        val container = findViewById<FrameLayout>(R.id.webViewContainer)
        refreshLayout.addView(webView)
        container.addView(refreshLayout)

        setupWebView()
        checkUpdate()

        refreshLayout.setOnRefreshListener {
            webView.reload()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.allowFileAccess = true
        settings.allowContentAccess = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true

        webView.addJavascriptInterface(WebAppInterface(this), "Android")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                refreshLayout.isRefreshing = false
                injectCustomJs()
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                refreshLayout.isRefreshing = false
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                android.app.AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                    .show()
                return true
            }

            override fun onJsConfirm(view: WebView?, url: String?, message: String?, result: JsResult?): Boolean {
                android.app.AlertDialog.Builder(this@MainActivity)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { _, _ -> result?.confirm() }
                    .setNegativeButton(android.R.string.cancel) { _, _ -> result?.cancel() }
                    .show()
                return true
            }
        }

        webView.loadUrl(BASE_URL)
    }

    private fun injectCustomJs() {
        webView.evaluateJavascript("""
            window.androidPlatform = true;
            window.systemName = 'Android';
        """.trimIndent(), null)
    }

    private fun checkUpdate() {
        UpdateChecker.check(this, "3.6.3", object : UpdateChecker.OnUpdateListener {
            override fun onUpdateAvailable(version: String, changelog: String, apkUrl: String) {
                showUpdateDialog(version, changelog, apkUrl)
            }

            override fun onNoUpdate() {}
        })
    }

    private fun showUpdateDialog(version: String, changelog: String, apkUrl: String) {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.update_title))
            .setMessage(String.format(getString(R.string.update_message), version) + "\n\n" + String.format(getString(R.string.update_changelog), changelog))
            .setPositiveButton(getString(R.string.update_now)) { _, _ ->
                downloadApk(apkUrl)
            }
            .setNegativeButton(getString(R.string.update_later), null)
            .show()
    }

    private fun downloadApk(url: String) {
        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(R.string.downloading)
            .setMessage("0%")
            .setCancelable(false)
            .create()
        dialog.show()

        Thread {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "LingLongHu.apk")
                val inputStream = response.body?.byteStream()
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(8192)
                var bytesRead: Int
                var totalBytes = 0L
                val contentLength = response.body?.contentLength() ?: 0L

                while (inputStream?.read(buffer).also { bytesRead = it ?: -1 } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                    totalBytes += bytesRead
                    val progress = ((totalBytes * 100) / contentLength).toInt()
                    runOnUiThread {
                        dialog.setMessage(String.format(getString(R.string.downloading), progress))
                    }
                }

                outputStream.close()
                inputStream?.close()

                runOnUiThread {
                    dialog.setMessage(getString(R.string.download_complete))
                }

                installApk(file)
                dialog.dismiss()

            } catch (e: IOException) {
                Log.e("MainActivity", "Download failed", e)
                runOnUiThread {
                    dialog.dismiss()
                    android.app.AlertDialog.Builder(this)
                        .setMessage("下载失败，请重试")
                        .setPositiveButton(android.R.string.ok, null)
                        .show()
                }
            }
        }.start()
    }

    private fun installApk(file: File) {
        val uri = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            androidx.core.content.FileProvider.getUriForFile(this, "com.linglonghu.dashboard.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        startActivity(intent)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
