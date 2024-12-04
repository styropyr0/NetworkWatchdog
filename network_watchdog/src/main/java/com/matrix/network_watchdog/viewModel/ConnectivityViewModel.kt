package com.matrix.network_watchdog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.network_watchdog.NetworkWatchdog
import com.matrix.network_watchdog.data.NetworkParams
import com.matrix.network_watchdog.interfaces.ConnectedListener
import com.matrix.network_watchdog.interfaces.ConnectedNoInternetAccessListener
import com.matrix.network_watchdog.interfaces.DisconnectedListener
import com.matrix.network_watchdog.interfaces.MeteredConnectionListener
import com.matrix.network_watchdog.interfaces.VPNListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn

/**
 * @author Saurav Sajeev | 04 Dec 2024
 */

class ConnectivityViewModel(private val networkWatchdog: NetworkWatchdog) : ViewModel() {

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Disconnected)

    var onConnected: ConnectedListener? = null
    var onDisconnected: DisconnectedListener? = null
    var onNoInternetAccess: ConnectedNoInternetAccessListener? = null
    var vpnConnectionListener: VPNListener? = null
    var meteredConnectionListener: MeteredConnectionListener? = null

    /**
     * Watch for network changes in background.
     * @param onConnected The ConnectedListener implementation which is invoked when internet is connected.
     * @param onDisconnected The DisconnectedListener implementation which is invoked when internet is disconnected.
     * @param onNoInternetAccess The ConnectedNoInternetAccessListener implementation which is invoked when connected but internet isn't accessible.
     * @param onMeteredConnection The MeteredConnectionListener implementation which is invoked when a metered connection is detected.
     * @param onVPNConnection The VPNListener implementation which is invoked when a VPN connection is detected.
     */
    fun startWatching(
        onConnected: ConnectedListener? = null,
        onDisconnected: DisconnectedListener? = null,
        onNoInternetAccess: ConnectedNoInternetAccessListener? = null,
        onMeteredConnection: MeteredConnectionListener? = null,
        onVPNConnection: VPNListener? = null
    ) {
        this.onConnected = onConnected
        this.onDisconnected = onDisconnected
        this.onNoInternetAccess = onNoInternetAccess
        this.meteredConnectionListener = onMeteredConnection
        this.vpnConnectionListener = onVPNConnection

        networkWatchdog.observeNetworkState(
            onConnected = {
                _networkState.value = NetworkState.Connected
                onConnected?.setOnConnectedListener()
            },
            onDisconnected = {
                _networkState.value = NetworkState.Disconnected
                onDisconnected?.setOnDisconnectedListener()
            },
            onNoInternetAccess = {
                _networkState.value = NetworkState.NoInternetAccess
                onNoInternetAccess?.setConnectedNoInternetAccessListener()
            },
            onMeteredConnection = {
                _networkState.value = NetworkState.MeteredConnection
                onMeteredConnection?.setMeteredConnectionListener()
            },
            onVPNConnection = {
                _networkState.value = NetworkState.VPNConnection
                onVPNConnection?.setVPNConnectionListener()
            }
        ).launchIn(viewModelScope)
    }

    /**
     * Stop the NetworkWatchdog from listening to network changes.
     * You may call this in your activity's onDestroy method
     */
    fun stopWatching() {
        networkWatchdog.stopWatching()
    }

    override fun onCleared() {
        super.onCleared()
        stopWatching()
    }

    /**
     * Access parameters about the connected network and last connected network.
     * @return NetworkParams object which contains connection status, network parameters, link properties, etc.
     */
    fun accessNetworkParams() = NetworkParams(
        isConnected = networkWatchdog.isNetworkConnected(),
        isMetered = networkWatchdog.isMeteredConnection(),
        isInternetAccessible = networkWatchdog.isInternetAccessible(),
        isConnectedToVPN = networkWatchdog.isConnectedToVPN(),
        currentNetwork = networkWatchdog.getCurrentNetwork(),
        lastNetwork = networkWatchdog.getLastNetwork(),
        linkProperties = networkWatchdog.getLinkProperties()
    )

}

/**
 * The NetworkWatchdog listens for changes with these data objects. You can use it to check for the status reported by the watchdog.
 */
sealed class NetworkState {
    data object Connected : NetworkState()
    data object Disconnected : NetworkState()
    data object NoInternetAccess : NetworkState()
    data object MeteredConnection : NetworkState()
    data object VPNConnection : NetworkState()
}
