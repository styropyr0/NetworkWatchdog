package com.matrix.network_watchdog.data

import android.net.LinkProperties
import android.net.Network

data class NetworkParams(
    val isConnected: Boolean,
    val isMetered: Boolean,
    val isInternetAccessible: Boolean,
    val isConnectedToVPN: Boolean,
    val lastNetwork: Network?,
    val currentNetwork: Network?,
    val linkProperties: LinkProperties?
)
