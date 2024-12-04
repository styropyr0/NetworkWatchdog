package com.matrix.network_watchdog

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.util.Log
import com.matrix.network_watchdog.viewModel.NetworkState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class NetworkWatchdog(private val context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    private var isWatching = false

    internal fun observeNetworkState(
        onConnected: () -> Unit = {},
        onDisconnected: () -> Unit = {},
        onNoInternetAccess: () -> Unit = {},
        onMeteredConnection: () -> Unit = {},
        onVPNConnection: () -> Unit = {}
    ): Flow<NetworkState> = callbackFlow {

        if (isWatching) close()
        isWatching = true

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(NetworkState.Connected).isSuccess
                Log.d("NetworkWatchdog", "Network is available")
                onConnected()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(NetworkState.Disconnected).isSuccess
                Log.d("NetworkWatchdog", "Network is lost")
                onDisconnected()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                val hasInternet =
                    networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val isVPN =
                    !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                val isMetered =
                    !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                trySend(if (hasInternet) NetworkState.Connected else NetworkState.NoInternetAccess).isSuccess
                Log.d("NetworkWatchdog", "Network capabilities changed: $hasInternet")

                if (!hasInternet) onNoInternetAccess()
                else onConnected()

                if (isVPN) onVPNConnection()

                if (isMetered) onMeteredConnection()
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)

        awaitClose {
            stopWatching()
        }
    }

    internal fun stopWatching() {
        if (isWatching) {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(it)
            }
            isWatching = false
        }
    }
}

