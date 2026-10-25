package com.nex.football

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.webkit.*

/**
 * Native host for NEX FOOTBALL.
 *
 * Every screen, colour, animation and interaction is the same web UI that ships today
 * (assets/index.html), rendered by the platform WebView - so the design is preserved 1:1.
 * Kotlin adds what the browser cannot do: Keystore key storage, FCM push, permissions,
 * photo/file upload, share, and back-button handling.
 */
class MainActivity : Activity() {

    private lateinit var web: WebView
    private lateinit var server: AssetServer
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private val FILE_CHOOSER = 7100

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var startUrl = "https://bcyyyfqmcapzqulkokyb.supabase.co/functions/v1/app-html"
        try {
            server = AssetServer(assets)
            server.start()
            startUrl = "http://127.0.0.1:" + server.port + "/index.html"
        } catch (e: Exception) {
            // local asset server unavailable -> fall back to the hosted app
        }

        web = WebView(this).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.allowFileAccess = true
            settings.mediaPlaybackRequiresUserGesture = false
            settings.useWideViewPort = true
            settings.loadWithOverviewMode = true
            settings.setSupportZoom(false)
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
            addJavascriptInterface(NexBridge(this@MainActivity), "NexBridge")

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView, request: WebResourceRequest, error: WebResourceError
                ) {
                    if (request.isForMainFrame) {
                        // keep the UI usable: show why, and offer the hosted app
                        view.loadData(
                            "<html><body style=\"background:#02061B;color:#E8EDF5;font-family:sans-serif;" +
                            "display:flex;align-items:center;justify-content:center;height:100vh;margin:0;text-align:center\">" +
                            "<div><h3>Could not load NEX FOOTBALL</h3><p style=\"color:#A8B3C2\">" +
                            "Check your internet connection, then reopen the app.</p></div></body></html>",
                            "text/html", "UTF-8"
                        )
                    }
                }
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val url = request.url
                    val host = url.host ?: ""
                    if (host == "127.0.0.1" || host.endsWith("supabase.co") ||
                        host.endsWith("github.io") || host == "localhost") return false
                    return try {
                        startActivity(Intent(Intent.ACTION_VIEW, url)); true
                    } catch (e: Exception) { true }
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    request.grant(request.resources)
                }
                override fun onShowFileChooser(
                    v: WebView?, cb: ValueCallback<Array<Uri>>?, params: FileChooserParams?
                ): Boolean {
                    filePathCallback?.onReceiveValue(null)
                    filePathCallback = cb
                    return try {
                        startActivityForResult(params!!.createIntent(), FILE_CHOOSER); true
                    } catch (e: Exception) { filePathCallback = null; false }
                }
            }
            loadUrl(startUrl)
        }
        setContentView(web)
    }

    override fun onBackPressed() {
        web.evaluateJavascript(
            "(function(){try{return (window.NEX_CAN_BACK&&window.NEX_CAN_BACK())?'1':'0'}catch(e){return '0'}})()"
        ) { result ->
            if (result != null && result.contains("1")) {
                web.evaluateJavascript("window.NEX_GO_BACK&&window.NEX_GO_BACK()", null)
            } else {
                finish()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == FILE_CHOOSER) {
            filePathCallback?.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data))
            filePathCallback = null
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        try { server.stop() } catch (e: Exception) {}
        super.onDestroy()
    }
}
