package com.yourname.habittracker.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.widget.Toast

class SimpleNetworkMonitor(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            // Показываем Toast при подключении к сети
            showToast("📶 Интернет подключен")
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            // Показываем Toast при отключении от сети
            showToast("❌ Интернет отключен")
        }
    }

    fun startMonitoring() {
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        // Показываем текущий статус при запуске
        showCurrentNetworkStatus()
    }

    fun stopMonitoring() {
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    private fun showCurrentNetworkStatus() {
        val isConnected = isNetworkAvailable()
        val message = if (isConnected) {
            "📶 Интернет подключен (при запуске)"
        } else {
            "❌ Интернет отключен (при запуске)"
        }
        showToast(message)
    }

    private fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                )
    }

    private fun showToast(message: String) {
        // Используем главный поток для показа Toast
        android.os.Handler(context.mainLooper).post {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
}