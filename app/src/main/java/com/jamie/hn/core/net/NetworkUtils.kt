package com.jamie.hn.core.net

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.TRANSPORT_CELLULAR
import android.net.NetworkCapabilities.TRANSPORT_WIFI

class NetworkUtils(
    private val context: Context
) {

    fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }
}
