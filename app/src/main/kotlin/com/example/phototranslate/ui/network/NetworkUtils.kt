package com.example.phototranslate.ui.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build

/**
 * NetworkUtils - Simple network connectivity checking.
 * Used to determine if the device has internet access before attempting
 * model downloads or online translation operations.
 */
object NetworkUtils {

    /**
     * Check if the device has an active internet connection.
     * Works on Android API 21+.
     */
    fun hasInternetConnection(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = cm.activeNetwork
            if (network == null) return false
            val capabilities = cm.getNetworkCapabilities(network)
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val netInfo = cm.activeNetworkInfo
            return netInfo != null && netInfo.isConnected
        }
    }

    /**
     * Check if we are using WiFi (as opposed to mobile data).
     */
    fun isWifiConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        @Suppress("DEPRECATION")
        val networkInfo = cm.activeNetworkInfo
        return networkInfo?.type == ConnectivityManager.TYPE_WIFI
    }
}
