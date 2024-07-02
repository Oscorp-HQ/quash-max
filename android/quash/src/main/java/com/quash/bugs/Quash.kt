/*
 * Copyright (c) 2024 Quash.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.quash.bugs

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.annotation.Keep
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.data.repository.QuashAppRegistrationRepository
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.core.flowcontrol.QuashFlowManager
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.di.component.QuashRecorderComponent
import com.quash.bugs.features.QuashAppRegistrar
import com.quash.bugs.features.crash.QuashCrashHandler
import com.quash.bugs.features.network.IQuashNetworkLogger
import com.quash.bugs.features.recorder.QuashRecorder
import com.quash.bugs.features.shake.ShakeDetector
import com.quash.bugs.presentation.bugreport.activity.QuashBugReportActivity
import com.quash.bugs.presentation.permission.PermissionRequestActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider

/**
 * Main class for the Quash SDK, responsible for initializing and managing various features.
 *
 * @property context the application context
 * @property quashAppPreferences instance of QuashAppPreferences for managing app preferences
 * @property appRegistrationRepository repository for registering the app
 * @property coroutineScope scope for managing coroutines
 * @property IQuashNetworkLogger logger for network activities
 * @property shakeDetectorProvider provider for the shake detector
 * @property recorderFactory factory for creating instances of QuashRecorder
 * @property navigationManager manager for handling navigation within the app
 */
class Quash @Inject constructor(
    private val context: Context,
    private val quashAppPreferences: QuashAppPreferences,
    private val appRegistrationRepository: QuashAppRegistrationRepository,
    private val coroutineScope: CoroutineScope,
    private val iQuashNetworkLogger: IQuashNetworkLogger,
    private val shakeDetectorProvider: Provider<ShakeDetector>,
    private val recorderFactory: QuashRecorderComponent.Factory,
    private val navigationManager: QuashFlowManager
) {

    private var lastShakeTime: Long = 0
    private val SHAKE_THRESHOLD_TIME = 3000

    private val shakeDetector by lazy { shakeDetectorProvider.get() }

    private lateinit var quashRecorder: QuashRecorder

    /**
     * Gets the network interceptor for logging network activities.
     */
    fun getNetworkInterceptor(): Interceptor? {
        return iQuashNetworkLogger.getNetworkInterceptor()
    }

    private val sdkInitializer: QuashAppRegistrar by lazy {
        QuashAppRegistrar(
            quashAppPreferences,
            appRegistrationRepository,
            coroutineScope,
            context
        )
    }

    @Keep
    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var INSTANCE: Quash? = null

        val context: Context
            get() = INSTANCE?.context
                ?: throw IllegalStateException("Quash not initialized - context is unavailable")

        @JvmStatic
        fun getInstance(): Quash =
            INSTANCE ?: throw IllegalStateException("Quash not initialized")

        /**
         * Initializes the Quash SDK.
         *
         * @param context the application context
         * @param applicationKey the application key for initialization
         * @param enableNetworkLogging flag to enable or disable network logging
         * @param sessionLength the duration of the session
         */
        fun initialize(
            context: Context,
            applicationKey: String,
            enableNetworkLogging: Boolean,
            sessionLength: Int = 40
        ) {
            INSTANCE ?: synchronized(this) {
                Log.d("QuashSDK", "Quash SDK Initializing")
                if (INSTANCE == null) {
                    INSTANCE = DaggerQuashComponent.builder()
                        .quashCoreModule(QuashCoreModule(context.applicationContext as Application))
                        .build()
                        .getQuash().apply {
                            iQuashNetworkLogger.initializeLogging(enableNetworkLogging)
                            sdkInitializer.initializeSDK(applicationKey, context.packageName,
                                onSuccess = {
                                    Log.d("QuashSDK", "Quash App registered successfully")
                                    initializeQuashRecorder(sessionLength = sessionLength)
                                    registerCrashListener(context)
                                    initShakeDetector()
                                    startRequiredServices()
                                    initFirebase(context)
                                    Log.d("QuashSDK", "Quash SDK & components initialized")
                                },
                                onFailure = { error ->
                                    Log.d("QuashSDK", "sdkInitializer failure: $error")
                                }
                            )
                        }
                } else {
                    Log.d("QuashSDK", "Quash SDK already initialized")
                }
            }
        }
    }

    /**
     * Initializes the shake detector for detecting shake gestures.
     */
    private fun initShakeDetector() {
        Log.d("QuashSDK", "initShakeDetector")
        coroutineScope.launch(Dispatchers.Main) {
            try {
                delay(1000)  // Delay for 1 second
                shakeDetector.startListening()
            } catch (e: Exception) {
                Log.e("QuashSDK", "Error starting shake detector", e)
            }
        }
    }

    /**
     * Initializes the QuashRecorder for capturing screenshots.
     *
     * @param initialFrequency the initial frequency of capturing screenshots
     * @param initialQuality the initial quality of screenshots
     * @param sessionLength the length of the session in seconds
     */
    private fun initializeQuashRecorder(
        initialFrequency: QuashRecorder.CaptureFrequency = QuashRecorder.CaptureFrequency.MEDIUM,
        initialQuality: QuashRecorder.ScreenshotQuality = QuashRecorder.ScreenshotQuality.MEDIUM,
        sessionLength: Int
    ) {
        quashAppPreferences.setSessionDuration(sessionLength)
        val recorderComponent =
            recorderFactory.create(initialQuality, initialFrequency, sessionLength)
        quashRecorder = recorderComponent.getRecorder()

        QuashAppLifeCycleCallbacks.initialize(
            context.applicationContext as Application,
            quashRecorder
        )
        Log.d("QuashSDK", " QuashAppLifeCycleCallbacks.initialize")
    }

    /**
     * Initializes Firebase with Crashlytics for the Quash SDK.
     *
     * @param context the application context
     */
    private fun initFirebase(context: Context) {
        val options = FirebaseOptions.Builder()
            .setApplicationId(BuildConfig.FIREBASE_APP_ID)
            .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
            .setApiKey(BuildConfig.FIREBASE_API_KEY)
            .build()

        FirebaseApp.initializeApp(context, options, "Quash-Android-SDK")
        Log.d("QuashSDK", "Initializing Firebase for Quash SDK")
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        Log.d("QuashSDK", "Firebase initialized, Crashlytics enabled")
    }

    /**
     * Registers a crash listener to handle uncaught exceptions.
     *
     * @param context the application context
     */
    private fun registerCrashListener(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler(QuashCrashHandler(context, navigationManager))
    }

    /**
     * Calculates the buffer size required for the session based on duration and frame rate.
     *
     * @param duration the duration of the session in seconds
     * @param frameRate the frame rate of capturing screenshots
     * @return the calculated buffer size
     */
    private fun calculateBufferSize(duration: Int, frameRate: Long): Int {
        return (duration * 1000 / frameRate).toInt() // Calculate buffer size based on duration and frame rate
    }

    /**
     * Starts required services for the Quash SDK.
     */
    private fun startRequiredServices() {
        coroutineScope.launch {
            delay(2000)
            QuashAppLifeCycleCallbacks.currentActivity?.let { activity ->
                activity.startActivity(Intent(activity, PermissionRequestActivity::class.java))
            } ?: Log.d("QuashSDK", "QuashAppLifeCycleCallbacks.currentActivity is null")
        }
    }

    /**
     * Handles shake detection and captures a screenshot when a shake is detected.
     */
    @SuppressLint("NewApi")
    fun onShakeDetected() {
        if (!::quashRecorder.isInitialized) {
            Log.d("QuashSDK", "quashRecorder !isInitialized on shake() ")
            return
        }
        Log.d("QuashSDK", "onShakeDetected")
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastShakeTime > SHAKE_THRESHOLD_TIME) {
            lastShakeTime = currentTime
            quashRecorder.captureScreenshot()?.let { screenshot ->
                saveScreenshotToFile(screenshot)?.let { uri ->
                    iQuashNetworkLogger.saveNetworkLogsToFile()
                    openReportActivity(uri)
                }
            }
        }
    }

    /**
     * Saves the captured screenshot to a file and returns the file URI.
     *
     * @param bitmap the screenshot bitmap
     * @return the URI of the saved screenshot file, or null if the save operation fails
     */
    private fun saveScreenshotToFile(bitmap: Bitmap): Uri? {
        return try {
            val file = File(context.cacheDir, "screenshot_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                Uri.fromFile(file)
            }
        } catch (e: IOException) {
            Log.e("QuashSDK", "Failed to save screenshot", e)
            null
        }
    }

    /**
     * Opens the bug report activity with the provided screenshot URI.
     *
     * @param uri the URI of the screenshot to include in the bug report
     */
    private fun openReportActivity(uri: Uri) {
        Intent(context, QuashBugReportActivity::class.java).also { intent ->
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("mediaUriShake", uri.toString())
            context.startActivity(intent)
        }
    }

    /**
     * Clears all network logs.
     */
    fun clearNetworkLogs() {
        iQuashNetworkLogger.clearNetworkLogs()
    }

    /**
     * Saves the file path for the network logs.
     */
    fun saveFilePathForNetwork() {
        iQuashNetworkLogger.saveNetworkLogsToFile()
    }
}
