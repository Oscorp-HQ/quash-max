# Official Android SDK for [Quash](https://quashbugs.com/)

Welcome to the Quash Android SDK, your ultimate in-app bug reporting tool! Built by developers for developers, this SDK captures everything you need to start fixing issues right away. It records crash logs, session replays, network logs, device information, and much more, ensuring you have all the details at your fingertips.

<p align="center">
    <a href="https://quash.io/docs/android-sdk/">
        <img src="https://storage.googleapis.com/misc_quash_static/android-sdk-png.png"/>
    </a>
</p>

<p align="center">
    <a href="https://android-arsenal.com/api?level=24"><img alt="API" src="https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat"/></a>
    <a href="https://github.com/Oscorp-HQ/quash-android-sdk/releases"><img src="https://img.shields.io/github/v/release/Oscorp-HQ/quash-android-sdk" /></a>
</p>

## Features

- Crash Logs: Automatically capture and log crashes in your application.
- Session Replays: Record user sessions to understand the steps leading to an issue.
- Network Logs: Capture all network requests and responses.
- Device Information: Collect detailed device information including OS version, device model, and more.
- Customizable Bug Reports: Allow users to report issues with custom fields and attachments.

## Table of Contents
1. [Setup](#setup)
    - [Manual Import](#manual-import)
    - [Configuration with quash.properties](#configuration-with-quashproperties)
2. [Installation](#installation)
3. [SDK Initialization](#sdk-initialization)
4. [IP Address Configuration for Testing](#ip-address-configuration-for-testing)
5. [Intercepting Network Requests](#intercepting-network-requests)
6. [Activation Mechanics](#activation-mechanics)
7. [Adding Firebase Plugins](#adding-firebase-plugins)
8. [Contributing](#contributing)

## Setup

### Manual Import

Since the Quash SDK is not available via Maven Central, users will need to manually import the Quash SDK along with its supporting modules into their Android projects. Follow the steps below to set up the SDK:

1. **Clone or Download the SDK:**
   - Clone the repository using Git:
     ```bash
     git clone https://github.com/your-repo/quash-android-sdk.git
     ```
   - Or download the repository as a ZIP file and extract it.

2. **Add the SDK as a Module:**
   - Open your Android project in Android Studio.
   - Go to **File > New > Import Module**.
   - Navigate to the `android` directory within the cloned or downloaded SDK.

3. **Update `settings.gradle`:**
   In your project's `settings.gradle` file, include the necessary modules:
   ```groovy
   include ':app', ':quash', ':quash-sdk-core', ':quash-sdk-features'
   ```

4. **Add Dependency in `build.gradle`:**
   In your app module's `build.gradle` file, add the Quash SDK as a dependency:
   ```groovy
   implementation(project(":quash"))
   ```
   **Note:** The `quash` module depends on two other modules: `quash-sdk-core` and `quash-sdk-features`. Ensure that both of these modules are also included in your project, as shown in the `settings.gradle` file.

5. **Sync Your Project:**
   Sync your project with Gradle files to ensure everything is set up correctly.

### Configuration with `quash.properties`

Each module within the SDK includes a `quash.properties` file that you need to configure. Here's how to set it up:

1. **Locate the File:**
   - Find the `quash.properties.example` file in each module's directory.
   - Specifically for the Quash module, locate the `google-service.json.example` file.

2. **Configure the Files:**
   - Rename `quash.properties.example` to `quash.properties` and edit it with your specific configuration parameters such as API keys or server URLs.
   - In the Quash module, rename `google-service.json.example` to `google-service.json` and update it according to your Firebase configuration needs.

```properties
# Example configuration entries
FirebaseApiKey=YOUR_FIREBASE_API_KEY
FirebaseAppId=YOUR_FIREBASE_APP_ID
FirebaseProjectId=YOUR_FIREBASE_PROJECT_ID

# Base URLs for different build environments
ReleaseBaseUrl=https://your_release_base_url.com
DebugBaseUrl=https://your_debug_base_url.com
```

**Secure Configuration:**
- Make sure to not expose sensitive information like API keys in public repositories. Use environment variables for sensitive data.

## Installation

### **Dependencies**

Ensure your Android project is compatible with the Quash SDK by setting the **`minSdkVersion`** to 24 or higher. This requirement supports advanced functionalities such as session replay processing and encoding.

To resolve the Quash SDK dependency, your project needs access to the appropriate repositories. Add the following repository configuration to your project's **`build.gradle`** (Project level):

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
    }
}
```
Next, add the Quash SDK to your project by including the following line in your build.gradle (Module level) file:

```
implementation 'com.quashbugs:sherlock:<VERSION>'
```
Replace <VERSION> with the desired version of the Quash Android SDK.

This setup ensures that your build environment can fetch and manage all necessary dependencies from Maven Central, Google's repository, and other specified sources, facilitating a smooth integration process.

## SDK Initialization

Initialize the SDK at the beginning of your application's lifecycle, ideally in the **`onCreate`** method of your **`Application`** class:

```kotlin
Quash.initialize(
    context = this,
    applicationKey = "YOUR_APPLICATION_KEY",
    shouldReadNetworkLogs = true/false
);
```
**Parameters:**

- **Context (`this`)**: The application context.
- **Application Key**: A unique identifier generated for your application, used to authenticate your app with Quash services.
- **Network Logs Flag (`true`)**: Enables network logging to capture all network traffic, which can be crucial for debugging network-related issues.

## IP Address Configuration for Testing

When testing the Quash SDK in conjunction with your backend, the IP address configuration is essential, especially depending on whether you're using an emulator, a real device, or a remote server.

### Emulator Setup
- **Use Special IP Address `10.0.2.2`:** If you're testing on an Android emulator, use the IP address `10.0.2.2` to access your local machine's localhost (`127.0.0.1`). This IP address maps the emulator's network to your development machine's localhost.
  **Example:**
  ```properties
  DebugBaseUrl=http://10.0.2.2:8080/
  ```
- **Port Configuration:** Ensure that the port you use in the URL corresponds to the port on which your local backend server is running.

### Real Device Setup
- **Use Your Local Machine's IP Address:** When testing on a real Android device, `localhost` refers to the device itself, not your development machine. To access your local backend from the device, you need to use your machine's local IP address.
  **Steps:**
  1. **Find Your Local IP Address:**
     - On macOS: Use the `ifconfig` command in Terminal.
     - On Windows: Use the `ipconfig` command in Command Prompt.
  2. **Update the Base URL:**
     ```properties
     DebugBaseUrl=http://192.168.1.100:8080/  // Replace with your actual local IP and port
     ```
  3. **Network Configuration:** Ensure that both your development machine and the Android device are on the same network. If using a firewall, allow connections on the relevant port.

### VM or External Backend Setup
- **Use External IP Address:** If your backend is hosted on a virtual machine (VM) instance or a remote server, you need to use the external IP address of that VM or server.
  **Example:**
  ```properties
  DebugBaseUrl=http://203.0.113.10:8080/  // Replace with your actual external IP and port
  ```
- **Clear Traffic Rules:**
  - **Firewall Rules:** Ensure that the firewall rules on the VM or server allow incoming traffic on the port your backend server is running.
  - **Network Security Configuration:** If using Android 9 or later, ensure your app's `network_security_config.xml` is set to allow cleartext traffic or use HTTPS for secure communication.
  **Example network security configuration:**
  ```xml
  <?xml version="1.0" encoding="utf-8"?>
  <network-security-config>
      <domain-config cleartextTrafficPermitted="true">
          <domain includeSubdomains="true">203.0.113.10</domain>  <!-- Replace with your external IP -->
      </domain-config>
  </network-security-config>
  ```

## Intercepting Network Requests

To intercept and log network requests and responses within your application, add Quash's network interceptor directly to your **`OkHttpClient`** configuration. Here's how you can set it up:

```kotlin
val builder = OkHttpClient.Builder()
Quash.getInstance().networkInterceptor?.let { interceptor ->
    builder.addInterceptor(interceptor)
}
val client = builder.build()
```

This setup configures your **`OkHttpClient`** to capture all network transactions, which are essential for debugging network issues. Integrating the interceptor directly ensures that every network request and response goes through Quash, allowing for detailed logging and analysis in your bug reports.

## Activation Mechanics

Quash SDK is activated by a default gesture: shaking the device. This gesture brings up the bug-reporting interface, allowing quick and easy bug reporting.

## Adding Firebase Plugins
We need firebase plugin to get internal checks on sdk
Depending on your build system setup, you might need to add Firebase plugins in your `build.gradle` files. Here are two ways to do it:

### Option 1: In the buildscript section

Add the Firebase plugins in the `buildscript` section of your root `build.gradle`:

```groovy
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.3.3'  // Google Services plugin
        classpath 'com.google.firebase:firebase-crashlytics-gradle:2.2.0'  // Crashlytics plugin
    }
}
```

### Option 2: In the plugins block

Alternatively, use the plugins DSL in your app module `build.gradle`:

```groovy
plugins {
    id 'com.google.gms.google-services' version '4.3.3' apply false
    id 'com.google.firebase.crashlytics' version '2.2.0' apply false
}
```
Apply the plugins at the end of the app module build.gradle:

```groovy
apply plugin: 'com.google.gms.google-services'
apply plugin: 'com.google.firebase.crashlytics'
```

## Contributing

We love contributions! Please read our
[contribution guidelines](/CONTRIBUTING.md) to get started.
