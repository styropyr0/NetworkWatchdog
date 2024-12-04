package com.matrix.network_watchdog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.network_watchdog.NetworkWatchdog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn

class ConnectivityViewModel(private val networkWatchdog: NetworkWatchdog) : ViewModel() {

    private val _networkState = MutableStateFlow<NetworkState>(NetworkState.Disconnected)
    val networkState: StateFlow<NetworkState> = _networkState.asStateFlow()

    private var onConnected: (() -> Unit)? = null
    private var onDisconnected: (() -> Unit)? = null
    private var onNoInternetAccess: (() -> Unit)? = null

    fun startWatching(
        onConnected: (() -> Unit)? = null,
        onDisconnected: (() -> Unit)? = null,
        onNoInternetAccess: (() -> Unit)? = null
    ) {
        this.onConnected = onConnected
        this.onDisconnected = onDisconnected
        this.onNoInternetAccess = onNoInternetAccess

        networkWatchdog.observeNetworkState(
            onConnected = {
                _networkState.value = NetworkState.Connected
                onConnected?.invoke()
            },
            onDisconnected = {
                _networkState.value = NetworkState.Disconnected
                onDisconnected?.invoke()
            },
            onNoInternetAccess = {
                _networkState.value = NetworkState.NoInternetAccess
                onNoInternetAccess?.invoke()
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
}
