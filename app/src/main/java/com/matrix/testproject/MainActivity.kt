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
        connectivityViewModel.startWatching(
            onConnected = { showStatus("Hey! You're online!", "#2CCE00") },
            onDisconnected = { showStatus("Oops! You lost the connection!", "#FF2701") },
            onNoInternetAccess = { showStatus("You have WiFi, but no actual internet!", "#DFC603") }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityViewModel.stopWatching() // Stop watching when the activity is destroyed
    }

    private fun showStatus(status: String, color: String) {
        runOnUiThread {
            findViewById<TextView>(R.id.status).apply{
                text = status
                setTextColor(Color.parseColor(color))
            }
        }
    }
}