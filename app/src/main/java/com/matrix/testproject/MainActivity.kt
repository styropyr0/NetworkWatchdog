package com.matrix.testproject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.matrix.network_watchdog.NetworkWatchdog
import com.matrix.network_watchdog.viewModel.ConnectivityViewModel
import com.matrix.network_watchdog.viewModel.ConnectivityViewModelFactory
import com.matrix.network_watchdog.viewModel.NetworkState
import kotlinx.coroutines.flow.collectLatest

class MainActivity : AppCompatActivity() {
    private val connectivityViewModel: ConnectivityViewModel by viewModels {
        ConnectivityViewModelFactory(NetworkWatchdog(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        connectivityViewModel.startWatching(
            onConnected = { showToast("Connected to the Internet") },
            onDisconnected = { showToast("Disconnected from the Internet") },
            onNoInternetAccess = { showToast("No Internet Access") }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityViewModel.stopWatching() // Stop watching when the activity is destroyed
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}