# **Network Watchdog Library**

**Latest version: 1.0.0**

**Network Watchdog** is a modern, lifecycle-aware library for monitoring network connectivity and internet availability in Android applications. It is designed using **Kotlin Coroutines**, **Jetpack ViewModel**, and **Kotlin Flow**, providing an efficient, thread-safe solution for network state management. The library integrates seamlessly into your app’s architecture, offering both **callback-based** and **observer-based** methods for monitoring network status.

---

## **Why Use Network Watchdog?**

### **Key Features**:

1. **Lifecycle-Aware Design**:  
   The library starts and stops network monitoring based on the lifecycle of your activities or fragments, ensuring minimal resource usage and avoiding memory leaks. It uses **ViewModel** to ensure network monitoring persists across configuration changes.

2. **Thread-Safe Updates**:  
   Network state changes are processed off the main thread using **Kotlin Flows**, keeping the main thread responsive. UI updates are safely handled using `runOnUiThread`.

3. **Comprehensive Internet Validation**:  
   Beyond basic connectivity checks, the library validates active internet access through **NetworkCapabilities**, ensuring that your app only treats the device as “connected” when it has actual internet access.

4. **State Management with ViewModel**:  
   Network states (`Connected`, `Disconnected`, `NoInternetAccess`, etc.) are exposed via a **StateFlow**, making it easy and efficient to observe network state changes directly from the UI layer.

5. **Optimized Resource Usage**:  
   The library caches the latest network state using `stateIn`, reducing redundant emissions and improving memory management. This ensures that your app doesn't perform unnecessary work when the network state hasn't changed.

6. **Dual Network Monitoring**:  
   **Callback-based** and **Observer-based** monitoring: The library supports both methods, so you can choose the most suitable one for your project. Callbacks provide direct actions for different network events, while `StateFlow` provides a declarative, lifecycle-aware method to monitor network changes.

7. **Modern Android Practices**:  
   The library adheres to Kotlin, Coroutines, and Jetpack components for a clean, maintainable, and scalable architecture.

### **Advantages Over Other Alternatives**:

While other solutions may be easier to implement, they often lack lifecycle awareness, don’t handle network state changes efficiently, or result in memory/resource leaks. **Network Watchdog** adheres to Android best practices, making it more robust and scalable.

---

## **Installation**

Add the following to your `build.gradle` (app-level):

### Gradle (App Level)

```gradle
dependencies {
    implementation 'com.github.styropyr0:styro-network-watchdog:1.0.0'
}
```

### Project-level `settings.gradle`

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

---

## **Getting Started**

### **Step 1: Initialize NetworkWatchdog**

Create an instance of `NetworkWatchdog` in your context:

```kotlin
val networkWatchdog = NetworkWatchdog(context)
```

### **Step 2: Integrate ConnectivityViewModel**

Use the **ConnectivityViewModel** to manage network state. Initialize it in your activity or fragment with the following factory method:

```kotlin
private val connectivityViewModel: ConnectivityViewModel by viewModels {
    ConnectivityViewModelFactory(NetworkWatchdog(this))
}
```

### **Step 3: Monitor Network State**

You can choose between **callback-based monitoring** or **StateFlow-based observation**.

#### **Method 1: Callback-based Network Monitoring**

Use `startWatching()` to provide specific actions based on the network state (e.g., connected, disconnected, no internet access):

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
    },
    onMeteredConnection = {
        runOnUiThread { showToast("Metered Connection") }
    },
    onVPNConnection = {
        runOnUiThread { showToast("VPN Connection") }
    }
)
```

#### **Method 2: StateFlow-based Network Monitoring**

Alternatively, you can use **StateFlow** for a more declarative approach to network monitoring. Observe network state changes in your UI layer with **lifecycleScope**:

```kotlin
lifecycleScope.launchWhenStarted {
    connectivityViewModel.networkState.collect { state ->
        runOnUiThread {
            when (state) {
                is NetworkState.Connected -> updateStatus("Connected")
                is NetworkState.Disconnected -> updateStatus("Disconnected")
                is NetworkState.NoInternetAccess -> updateStatus("No Internet Access")
                is NetworkState.Metered -> updateStatus("Metered Network")
                is NetworkState.VPN -> updateStatus("VPN Connected")
            }
        }
    }
}
```

---

## **Implementation Details**

### **Network Watchdog**

`NetworkWatchdog` uses **ConnectivityManager** and **NetworkCallback** to listen for network changes. It uses **Flow** to emit updates about network connectivity and internet access, ensuring the device is connected to the internet and not just to a local network.

- **onConnected**: Triggered when the device is connected to a network.
- **onDisconnected**: Triggered when the device disconnects from any network.
- **onNoInternetAccess**: Triggered when there is no internet access despite the device being connected to a network.
- **onMeteredConnection**: Triggered when the device is connected to a metered (pay-per-data) network.
- **onVPNConnection**: Triggered when the device is connected to a VPN.

### **ConnectivityViewModel**

- **StateFlow Integration**: Exposes network state changes as **StateFlow** for easy observation. It makes sure network state is always updated efficiently, and updates only when the app is in an active lifecycle state.
- **Caching**: The latest network state is cached using `stateIn`, which prevents redundant emissions and optimizes memory usage.
- **Lifecycle Awareness**: The **ConnectivityViewModel** automatically manages the start and stop of network monitoring based on the lifecycle of the activity/fragment.

### **UI Safety with `runOnUiThread`**

Since network monitoring and state updates are handled off the main thread, use `runOnUiThread` to ensure that UI updates happen on the main thread, preventing any threading issues when interacting with UI elements.

```kotlin
runOnUiThread {
    // Safely update UI elements
}
```

---

## **Advantages of This Approach**

- **Lifecycle Awareness**: Network monitoring automatically adjusts to the lifecycle of your components, ensuring no unnecessary resources are used.
- **Efficient State Management**: **StateFlow** and `stateIn` help to manage the network state efficiently by reducing redundant emissions.
- **Scalable and Maintainable**: This design enables you to easily scale network monitoring and integrate it into large applications.
- **Thread Safety**: Network monitoring runs off the main thread, ensuring your app remains responsive.

---

## **Example Usage**

Here's a complete example using **Network Watchdog** in an activity:

```kotlin
private val connectivityViewModel: ConnectivityViewModel by viewModels {
    ConnectivityViewModelFactory(NetworkWatchdog(this))
}

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    // Start watching network state
    connectivityViewModel.startWatching(
        onConnected = {
            runOnUiThread { showToast("Connected to the Internet") }
        },
        onDisconnected = {
            runOnUiThread { showToast("Disconnected from the Internet") }
        },
        onNoInternetAccess = {
            runOnUiThread { showToast("No Internet Access") }
        },
        onMeteredConnection = {
            runOnUiThread { showToast("Metered Connection") }
        },
        onVPNConnection = {
            runOnUiThread { showToast("VPN Connected") }
        }
    )

    // Collect network state with StateFlow
    lifecycleScope.launchWhenStarted {
        connectivityViewModel.networkState.collect { state ->
            runOnUiThread {
                when (state) {
                    is NetworkState.Connected -> updateStatus("Connected")
                    is NetworkState.Disconnected -> updateStatus("Disconnected")
                    is NetworkState.NoInternetAccess -> updateStatus("No Internet Access")
                    is NetworkState.Metered -> updateStatus("Metered Network")
                    is NetworkState.VPN -> updateStatus("VPN Connected")
                }
            }
        }
    }
}
```

---

## **Contribution**

Contributions are welcome! If you encounter any bugs or have feature requests, feel free to open issues or submit pull requests.

---

## **License**

This project is licensed under the **MIT License**. See the `LICENSE` file for details.

---

## **Notes**

- Always use **`runOnUiThread`** for UI updates inside network state callbacks.
- **`ConnectivityViewModel`** ensures lifecycle-safety and efficient network monitoring management.
