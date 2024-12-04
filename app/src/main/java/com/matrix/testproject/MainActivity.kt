package com.matrix.testproject

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
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
        val connectionSts = findViewById<TextView>(R.id.connectionStatus)
        val meteredSts = findViewById<TextView>(R.id.meteredStatus)
        val vpnSts = findViewById<TextView>(R.id.VPNStatus)

        connectivityViewModel.startWatching(
            onConnected = { showStatus(connectionSts, "Hey! You're online!", "#2CCE00") },
            onDisconnected = { showStatus(connectionSts, "Oops! You lost the connection!", "#FF2701") },
            onNoInternetAccess = { showStatus(connectionSts, "You have WiFi, but no actual internet!", "#DFC603") },
            onMeteredConnection = { showStatus(meteredSts, "You have a metered connection!", "#DFC603") },
            onVPNConnection = { showStatus(vpnSts, "You have a VPN connection!", "#DFC603") }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityViewModel.stopWatching()
    }

    private fun showStatus(textView: TextView, status: String, color: String) {
        runOnUiThread {
            textView.apply{
                text = status
                setTextColor(Color.parseColor(color))
            }
        }
    }
}