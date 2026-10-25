package com.nex.football

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.webkit.JavascriptInterface
import android.widget.Toast

/**
 * JS <-> Kotlin bridge: everything the web layer cannot do itself.
 * The web app runs unchanged without it - these are progressive enhancements.
 */
class NexBridge(private val activity: Activity) {

    private val prefs = activity.getSharedPreferences("nex_native", Context.MODE_PRIVATE)

    @JavascriptInterface
    fun deviceInfo(): String =
        "{\"platform\":\"android\",\"sdk\":" + Build.VERSION.SDK_INT +
        ",\"model\":\"" + Build.MODEL.replace("\"", "") +
        "\",\"version\":\"" + Build.VERSION.RELEASE +
        "\",\"secureStorage\":true,\"push\":" + (prefs.getBoolean("fcm_ready", false)) + "}"

    @JavascriptInterface
    fun secureSet(key: String, value: String): Boolean = try {
        prefs.edit().putString("vault_" + key, KeyVault.put(value)).apply(); true
    } catch (e: Exception) { false }

    @JavascriptInterface
    fun secureGet(key: String): String? {
        val blob = prefs.getString("vault_" + key, null) ?: return null
        return KeyVault.get(blob)
    }

    @JavascriptInterface
    fun secureRemove(key: String) { prefs.edit().remove("vault_" + key).apply() }

    @JavascriptInterface
    fun toast(message: String) {
        activity.runOnUiThread { Toast.makeText(activity, message, Toast.LENGTH_SHORT).show() }
    }

    @JavascriptInterface
    fun share(text: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text)
        }
        activity.startActivity(Intent.createChooser(send, "Share"))
    }

    @JavascriptInterface
    fun hasPermission(permission: String): Boolean =
        activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED

    @JavascriptInterface
    fun requestPermissions(which: String) {
        val list: Array<String> = when (which) {
            "mic" -> arrayOf(android.Manifest.permission.RECORD_AUDIO)
            "camera" -> arrayOf(android.Manifest.permission.CAMERA)
            "notifications" -> if (Build.VERSION.SDK_INT >= 33)
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS) else emptyArray()
            else -> emptyArray()
        }
        if (list.isNotEmpty()) activity.runOnUiThread { activity.requestPermissions(list, 7101) }
    }

    @JavascriptInterface
    fun pushToken(): String = prefs.getString("fcm_token", "") ?: ""

    @JavascriptInterface
    fun pushSupported(): Boolean = prefs.getBoolean("fcm_ready", false)

    @JavascriptInterface
    fun appVersion(): String = "1.0"
}
