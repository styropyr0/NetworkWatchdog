package com.matrix.network_watchdog.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.matrix.network_watchdog.NetworkWatchdog

/**
 * @author Saurav Sajeev | 04 Dec 2024
 */

class ConnectivityViewModelFactory(private val connectivityObserver: NetworkWatchdog) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectivityViewModel(connectivityObserver) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
