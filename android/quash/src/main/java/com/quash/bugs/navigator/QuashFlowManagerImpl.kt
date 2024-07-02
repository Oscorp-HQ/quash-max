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

package com.quash.bugs.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.view.View
import android.view.ViewGroup
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quash.bugs.QuashAppLifeCycleCallbacks
import com.quash.bugs.core.data.preference.QuashAppPreferences
import com.quash.bugs.core.flowcontrol.QuashFlowManager
import com.quash.bugs.core.util.QuashBitmapUtils
import com.quash.bugs.service.QuashWatchdogService

/**
 * Implementation of QuashServiceController, providing service start capabilities,
 * bitmap storage, and exception logging functionalities.
 */
class QuashFlowManagerImpl(
    private val context: Context,
    private val quashAppPreferences: QuashAppPreferences
) : QuashFlowManager {

    /**
     * Starts a service with custom configurations provided through a lambda expression.
     */
    override fun startService(intentSetup: (Intent) -> Unit) {
        val intent = Intent(context, QuashWatchdogService::class.java)
        intentSetup(intent)  // Configure the intent as needed before starting the service.
        context.startService(intent)
    }

    /**
     * Saves a bitmap image to a file and returns the file path.
     * Utilizes QuashBitmapSaver for file operations.
     */
    override fun saveBitmapToFile(bitmap: Bitmap): String {
        return QuashBitmapUtils.saveBitmapToFile(context, bitmap)
    }

    /**
     * Logs exceptions to Firebase Crashlytics and adds custom diagnostic data.
     */
    override fun logException(
        throwable: Throwable,
        threadName: String?
    ) {
        FirebaseCrashlytics.getInstance().apply {
            recordException(throwable)
            val additionalData = mapOf(
                "SDK_Version" to "BuildCon",
                "Device_Info" to getDeviceInfo(),
                "Organization_Key" to (quashAppPreferences.getOrgKey() ?: "Unknown"),
                "Battery_Level" to getBatteryLevel()
            )
            additionalData.forEach { (key, value) ->
                setCustomKey(key, value)
            }
            log("Custom crash report: SDK crashed")
            sendUnsentReports()
        }
    }

    /**
     * Retrieves the currently active activity using a lifecycle callback mechanism.
     * This method returns the activity currently in the foreground if available.
     *
     * @return The currently active Activity or null if no activity is active.
     */
    override fun getCurrentActivity(): Activity? {
        return QuashAppLifeCycleCallbacks.currentActivity
    }

    /**
     * Adds an overlay view to the current activity's root view.
     * This method is typically used for adding custom UI elements like floating buttons
     * or informational overlays that need to persist across the entire activity.
     *
     * @param overlayView The view to be added as an overlay.
     */
    override fun addOverlayToActivity(overlayView: View) {
        getCurrentActivity()?.let {
            val rootView = it.findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(
                overlayView, ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }
    }

    /**
     * Removes an overlay view from the current activity's root view.
     * This method is useful for cleaning up custom UI elements that were previously
     * added to the activity's view hierarchy.
     *
     * @param overlayView The view to be removed from the activity's layout.
     */
    override fun removeOverlayFromActivity(overlayView: View) {
        getCurrentActivity()?.let {
            val rootView = it.findViewById<ViewGroup>(android.R.id.content)
            rootView.removeView(overlayView)
        }
    }

    /**
     * Retrieves comprehensive device information for diagnostic purposes.
     */
    private fun getDeviceInfo(): String {
        return "Details about the device configuration."
    }

    /**
     * Retrieves the current battery level of the device.
     */
    private fun getBatteryLevel(): String {
        return "Battery level information."
    }
}
