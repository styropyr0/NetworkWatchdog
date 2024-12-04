# Network Watchdog

**Network Watchdog** is a modern, lifecycle-aware library for monitoring network connectivity and internet availability in Android applications. Designed using **Kotlin Coroutines** and **Jetpack ViewModel**, it seamlessly integrates with your app's architecture and lifecycle to provide efficient, reliable, and thread-safe network state updates.

---

## Why Use Network Watchdog?

### Key Features:
1. **Lifecycle-Aware Design**:  
   Automatically starts and stops network monitoring based on the lifecycle of your activities and fragments, ensuring optimal resource usage and avoiding memory leaks.

2. **Thread-Safe Updates**:  
   Network state changes are processed off the main thread using **Kotlin Flows**, ensuring smooth application performance. UI updates are easily integrated by explicitly using `runOnUiThread`.

3. **Comprehensive Internet Validation**:  
   Goes beyond basic connectivity checks by validating active internet access using `NetworkCapabilities`.

4. **State Management with ViewModel**:  
   Network states (`Connected`, `Disconnected`, `NoInternetAccess`) are exposed as a **StateFlow**, allowing easy and efficient observation in your UI layer.

5. **Optimized Resource Usage**:  
   Uses **stateIn** to cache the latest network state, reducing redundant emissions and enhancing memory management.

6. **Modern Android Development Practices**:  
   Incorporates Kotlin, Coroutines, Jetpack ViewModel, and StateFlow for a clean, maintainable, and scalable implementation.

### Advantages Over Other Alternatives:
While other network monitoring solutions might be easier to implement, they often lack proper lifecycle management, may not differentiate between connectivity and internet availability, and can lead to resource leaks. Network Watchdog adheres to Android best practices to provide a robust and lifecycle-aware solution.

---

## Installation

Ensure your `build.gradle` (app level) includes the following dependencies:
```gradle
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1" // ViewModel
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3" // Coroutines
```

---

## Getting Started

### Step 1: Initialize `NetworkWatchdog`
Create an instance of `NetworkWatchdog` in your context:
```kotlin
val networkWatchdog = NetworkWatchdog(context)
```

### Step 2: Integrate `ConnectivityViewModel`
The ViewModel handles network state updates. Use the provided factory to initialize it in your activity or fragment:
```kotlin
private val connectivityViewModel: ConnectivityViewModel by viewModels {
    ConnectivityViewModelFactory(NetworkWatchdog(this))
}
```

### Step 3: Observe Network State
Start monitoring the network state and handle UI updates safely:
```kotlin
connectivityViewModel.startWatching(
    onConnected = {
        runOnUiThread { showToast("Connected to the Internet") }
    },
    onDisconnected = {
        runOnUiThread { showToast("Disconnected from the Internet") }
    },
    onNoInternetAccess = {
        runOnUiThread { showToast("No Internet Access") }
    }
)

lifecycleScope.launchWhenStarted {
    connectivityViewModel.networkState.collect { state ->
        runOnUiThread {
            when (state) {
                is NetworkState.Connected -> updateUI("Connected")
                is NetworkState.Disconnected -> updateUI("Disconnected")
                is NetworkState.NoInternetAccess -> updateUI("No Internet Access")
            }
        }
    }
}
```

---

## Implementation Details

### Network Watchdog
Uses `ConnectivityManager` and `NetworkCallback` to monitor network changes. The `observeNetworkState` function emits connectivity updates through a **Flow**, including validation for active internet access.

### ConnectivityViewModel
- Encapsulates network state management using a **StateFlow**.
- Caches the latest network state using `stateIn`, ensuring efficient and lifecycle-aware updates.
- Automatically manages network monitoring when the ViewModel is created or cleared.

### StateFlow Integration
Network Watchdog uses **StateFlow**, a lifecycle-aware reactive stream, to provide real-time updates. Observers are only notified when the activity or fragment is active, reducing unnecessary processing.

### UI Safety with `runOnUiThread`
Network callbacks are invoked on a background thread. To avoid threading issues when interacting with UI elements, all UI updates must be executed inside `runOnUiThread`. Example:
```kotlin
runOnUiThread {
    // Safely update the UI
}
```

---

## Advantages of This Approach

- **Lifecycle Awareness**: Automatically adapts to the lifecycle of your components.
- **Efficient State Management**: Reduces overhead with `stateIn` and StateFlow.
- **Scalable and Maintainable**: Designed for clean integration into modern Android architectures.
- **Thread Safety**: Ensures that all background tasks are safely executed without affecting the main thread.

---

## Example Usage

The following demonstrates a complete integration:
```kotlin
private val connectivityViewModel: ConnectivityViewModel by viewModels {
    ConnectivityViewModelFactory(NetworkWatchdog(this))
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    connectivityViewModel.startWatching(
        onConnected = {
            runOnUiThread { showToast("Connected to the Internet") }
        },
        onDisconnected = {
            runOnUiThread { showToast("Disconnected from the Internet") }
        },
        onNoInternetAccess = {
            runOnUiThread { showToast("No Internet Access") }
        }
    )

    lifecycleScope.launchWhenStarted {
        connectivityViewModel.networkState.collect { state ->
            runOnUiThread {
                when (state) {
                    is NetworkState.Connected -> updateStatus("Connected")
                    is NetworkState.Disconnected -> updateStatus("Disconnected")
                    is NetworkState.NoInternetAccess -> updateStatus("No Internet Access")
                }
            }
        }
    }
}
```

---

## Contribution

Contributions are welcome. Feel free to open issues for bug reports or feature requests, and submit pull requests for improvements.

---

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.

---

## Notes

- Always use `runOnUiThread` for updating UI components in callbacks.
- Use `ConnectivityViewModel` to manage network monitoring efficiently and lifecycle-safely.
```
