# **Network Watchdog Library**

The **Network Watchdog** library is a robust solution for monitoring network state changes in Android applications. It provides a flexible and lifecycle-aware way to track changes in network connectivity, including whether the device is connected, if the connection is metered, if there is internet access, or if a VPN connection is active. The library leverages **ViewModel** and **LiveData/Flow** for managing state changes efficiently and reacts in real-time to network changes.

---

## **Key Features**

### **1. Lifecycle-Aware Network Monitoring**

- The library integrates seamlessly with Android's **ViewModel** to ensure that network monitoring is lifecycle-aware, which is a key benefit for long-running tasks. This means it listens for network changes while the app is running and automatically cleans up when the activity/fragment lifecycle ends (e.g., on `onDestroy`).

### **2. ViewModel Factory for Dependency Injection**
- The library uses a custom **ViewModel Factory** (`ConnectivityViewModelFactory`) to inject `NetworkWatchdog` into `ConnectivityViewModel`. This approach ensures that dependencies like `NetworkWatchdog` are properly passed to the ViewModel, without using global variables or static instances.
- This **ViewModel-based architecture** ensures that your network monitoring is encapsulated and reusable across different parts of the app, offering a clean separation of concerns.

### **3. Real-Time Network State Monitoring**

- **Network State**: Detects various states including:
   - **Connected**: The device is connected to a network.
   - **Disconnected**: No network connection is active.
   - **No Internet Access**: Connected to a network, but no internet access is available.
   - **Metered Connection**: Connection is metered (e.g., mobile data).
   - **VPN Connection**: The device is connected to a VPN.

### **4. Advanced Network Properties Access**
- The library provides access to detailed **network properties** such as link properties, current and last connected network, metered connection status, and VPN status. This allows you to manage network conditions in a more fine-grained manner.

---

## **Installation**

To include the **Network Watchdog** library in your project, follow the instructions below.

### **In your `build.gradle` (Project-level)**

Add JitPack repository to the `repositories` section if it's not already added.

```gradle
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

### **In your `build.gradle` (App-level)**

Add the following dependency to the `dependencies` section:

```gradle
dependencies {
    implementation 'com.github.styropyr0:styro-network-watchdog:1.0.0'
}
```

Or for **`build.gradle.kts`**:

```kotlin
dependencies {
    implementation("com.github.styropyr0:styro-network-watchdog:1.0.0")
}
```

### **In your `settings.gradle` (Project-level)**

Add the following to your `settings.gradle` to enable JitPack.

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

Or for **`settings.gradle.kts`**:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```
---

## **How to Use**

### **Step 1: Initializing `NetworkWatchdog`**

To begin using the library, create an instance of the `NetworkWatchdog` class by passing the application context. This class is responsible for monitoring network changes.

```kotlin
val networkWatchdog = NetworkWatchdog(context)
```

---

### **Step 2: Using `ConnectivityViewModel`**

The recommended approach is to use the `ConnectivityViewModel` to handle network monitoring in a lifecycle-aware manner. This ensures that network monitoring is linked to the ViewModel's lifecycle, which is automatically cleaned up when the ViewModel is destroyed.

#### **Setting up the ViewModel**

You will need to use a **ViewModel Factory** (`ConnectivityViewModelFactory`) to pass the `NetworkWatchdog` dependency into your `ConnectivityViewModel`.

```kotlin
private val connectivityViewModel: ConnectivityViewModel by viewModels {
    ConnectivityViewModelFactory(NetworkWatchdog(this))
}
```

---

### **Step 3: Start Monitoring Network State**

#### **Using Callbacks**

The library provides a function `startWatching` in the `ConnectivityViewModel` that allows you to register callback functions for different network states. Each callback will be triggered when the corresponding network state occurs.

```kotlin
connectivityViewModel.startWatching(
    onConnected = { showStatus("You're online!", "#2CCE00") },
    onDisconnected = { showStatus("You're offline!", "#FF2701") },
    onNoInternetAccess = { showStatus("Connected to a network, but no internet!", "#DFC603") },
    onMeteredConnection = { showStatus("Metered connection detected!", "#DFC603") },
    onVPNConnection = { showStatus("VPN connection active!", "#DFC603") }
)
```

This function will call the corresponding methods (`onConnected`, `onDisconnected`, etc.) when the network state changes, allowing you to handle each case differently.

#### **Network State Management**

Each callback method is responsible for updating your UI or performing actions based on the network state. Here's a breakdown of how the state is handled:

- **onConnected**: Triggered when the device is connected to a network.
- **onDisconnected**: Triggered when the device loses its network connection.
- **onNoInternetAccess**: Triggered when the device is connected to a network but cannot access the internet (i.e., no internet access).
- **onMeteredConnection**: Triggered when a metered network (like mobile data) is detected.
- **onVPNConnection**: Triggered when a VPN connection is detected.

---

### **Step 4: Access Network Parameters**

The `ConnectivityViewModel` provides the method `accessNetworkParams()`, which returns a `NetworkParams` object that contains detailed information about the current network. This includes:
- **Network connection status**: Whether the device is connected or disconnected.
- **Metered connection status**: Whether the device is on a metered connection.
- **Internet accessibility**: Whether the connected network has internet access.
- **VPN status**: Whether the device is connected to a VPN.
- **Link properties**: Advanced network parameters (e.g., IP addresses, DNS servers).

```kotlin
val networkParams = connectivityViewModel.accessNetworkParams()
println("Is Connected: ${networkParams.isConnected}")
println("Is Metered: ${networkParams.isMetered}")
println("Is Internet Accessible: ${networkParams.isInternetAccessible}")
println("VPN Status: ${networkParams.isConnectedToVPN}")
println("Link Properties: ${networkParams.linkProperties}")
```

---

### **Step 5: Stop Monitoring**

It’s important to stop the network monitoring when it's no longer needed to prevent memory leaks. You can call `stopWatching()` in your activity/fragment's `onDestroy` method:

```kotlin
override fun onDestroy() {
    super.onDestroy()
    connectivityViewModel.stopWatching()
}
```

---

## **Detailed Explanation of Key Functions**

### **1. `NetworkWatchdog.observeNetworkState`**
- **Purpose**: Monitors the network state and sends updates through a `Flow` object.
- **Parameters**:
   - Callbacks for each network state (connected, disconnected, no internet, metered, VPN, and link properties).
- **How it works**:
   - Registers a `NetworkCallback` with `ConnectivityManager` to listen for network state changes.
   - Calls appropriate callbacks based on the network status.
   - Uses `trySend` to emit network states through the `Flow` object.

### **2. `ConnectivityViewModel.startWatching`**
- **Purpose**: Sets up the network state monitoring and links it with the appropriate listener functions.
- **Parameters**:
   - Various listener callbacks (`onConnected`, `onDisconnected`, `onNoInternetAccess`, etc.).
- **How it works**:
   - Observes the network state changes from the `NetworkWatchdog` and invokes the corresponding callback functions.
   - Updates the internal `_networkState` to reflect the current network state, allowing the UI to react to changes.

### **3. `ConnectivityViewModel.accessNetworkParams`**
- **Purpose**: Retrieves a bundle of network details like connection status, metered status, internet access, VPN status, and link properties.
- **How it works**:
   - Returns a `NetworkParams` object that encapsulates detailed information about the current and last connected networks, metered connection status, and more.

### **4. `ConnectivityViewModel.stopWatching`**
- **Purpose**: Stops the `NetworkWatchdog` from listening to network state changes.
- **How it works**:
   - Unregisters the `NetworkCallback` from `ConnectivityManager`, effectively stopping the monitoring process.

---

## **Why Choose Network Watchdog?**

- **Lifecycle Awareness**: The library integrates with the **ViewModel** to manage network state monitoring in a lifecycle-aware manner. This ensures that resources are properly cleaned up when not needed.
- **Seamless Integration**: It works well within Android's architecture components, using **LiveData** and **Flow** for reactive programming.
- **Fine-Grained Control**: Offers detailed control over network events, including metered connections, VPN detection, and more.
- **Simple Dependency Injection**: Uses a custom **ViewModel Factory** to inject dependencies efficiently, ensuring modularity and separation of concerns.
- **Flexible Network Monitoring**: Supports both **callback-based** and **observer-based** (via LiveData/Flow) monitoring for maximum flexibility.
- **No Global Dependencies**: The use of ViewModel ensures that dependencies are scoped and do not rely on global variables or static contexts.

---

## **License**

This library is licensed under the **MIT License**.
