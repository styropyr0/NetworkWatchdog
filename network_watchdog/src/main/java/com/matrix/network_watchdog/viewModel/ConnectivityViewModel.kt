package com.matrix.network_watchdog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.network_watchdog.NetworkWatchdog
import com.matrix.network_watchdog.interfaces.ConnectedListener
import com.matrix.network_watchdog.interfaces.ConnectedNoInternetAccessListener
import com.matrix.network_watchdog.interfaces.DisconnectedListener
import com.matrix.network_watchdog.interfaces.MeteredConnectionListener
import com.matrix.network_watchdog.interfaces.VPNListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn

class ConnectivityViewModel(private val networkWatchdog: NetworkWatchdog) : ViewModel() {

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Disconnected)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    var onConnected: ConnectedListener? = null
    var onDisconnected: DisconnectedListener? = null
    var onNoInternetAccess: ConnectedNoInternetAccessListener? = null
    var vpnConnectionListener: VPNListener? = null
    var meteredConnectionListener: MeteredConnectionListener? = null

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

    fun stopWatching() {
        networkWatchdog.stopWatching()
    }

    override fun onCleared() {
        super.onCleared()
        stopWatching()
    }
}

sealed class NetworkState {
    data object Connected : NetworkState()
    data object Disconnected : NetworkState()
    data object NoInternetAccess : NetworkState()
    data object MeteredConnection : NetworkState()
    data object VPNConnection : NetworkState()
}
