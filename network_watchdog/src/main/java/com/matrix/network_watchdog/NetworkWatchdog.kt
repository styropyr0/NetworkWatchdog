package com.matrix.network_watchdog

import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
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

    private var isConnected = false
    private var isMetered = false
    private var isNoInternetAccess = false
    private var isConnectedToInternet = false
    private var isConnectedToVPN = false
    private var lastNetwork: Network? = null
    private var currentNetwork: Network? = null
    private var linkProperties: LinkProperties? = null

    private var isWatching = false

    internal fun observeNetworkState(
        onConnected: () -> Unit = {},
        onDisconnected: () -> Unit = {},
        onNoInternetAccess: () -> Unit = {},
        onMeteredConnection: () -> Unit = {},
        onVPNConnection: () -> Unit = {},
        onLinkPropertiesChanged: () -> Unit = {}
    ): Flow<NetworkState> = callbackFlow {

        if (isWatching) close()
        isWatching = true

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(NetworkState.Connected).isSuccess
                Log.d("NetworkWatchdog", "Network is available")
                isConnected = true
                if (currentNetwork != null)
                    lastNetwork = currentNetwork
                currentNetwork = network
                onConnected()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(NetworkState.Disconnected).isSuccess
                Log.d("NetworkWatchdog", "Network is lost")
                lastNetwork = currentNetwork
                onDisconnected()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                isConnectedToInternet =
                    !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                isConnectedToVPN =
                    !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
                isMetered =
                    !networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                trySend(if (isConnectedToInternet) NetworkState.Connected else NetworkState.NoInternetAccess).isSuccess
                Log.d("NetworkWatchdog", "Network capabilities changed: $isConnectedToInternet")

                if (currentNetwork != null)
                    lastNetwork = currentNetwork
                currentNetwork = network

                if (isConnectedToInternet) onNoInternetAccess()
                else onConnected()

                if (isConnectedToVPN) onVPNConnection()

                if (isMetered) onMeteredConnection()
            }

            override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) {
                super.onLinkPropertiesChanged(network, linkProperties)
                this@NetworkWatchdog.linkProperties = linkProperties
                onLinkPropertiesChanged()
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback!!)

        awaitClose {
            stopWatching()
        }
    }

    fun isNetworkConnected() = isConnected
    fun isMeteredConnection() = isMetered
    fun isNoInternetAccess() = isNoInternetAccess
    fun isConnectedToVPN() = isConnectedToVPN
    fun getLinkProperties(): LinkProperties? = linkProperties
    fun getCurrentNetwork(): Network? = currentNetwork
    fun getLastNetwork(): Network? = lastNetwork

    internal fun stopWatching() {
        if (isWatching) {
            networkCallback?.let {
                connectivityManager.unregisterNetworkCallback(it)
            }
            isWatching = false
        }
    }
}

