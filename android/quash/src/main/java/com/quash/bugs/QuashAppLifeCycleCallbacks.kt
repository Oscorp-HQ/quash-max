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

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.quash.bugs.features.recorder.QuashInteractionOverlayView
import com.quash.bugs.features.recorder.QuashRecorder
import com.quash.bugs.presentation.bugreport.activity.QuashBugReportActivity
import com.quash.bugs.presentation.permission.QuashCrashNotifierActivity
import java.lang.ref.WeakReference

/**
 * Singleton class to handle application lifecycle callbacks and manage screen recording.
 */
object QuashAppLifeCycleCallbacks : Application.ActivityLifecycleCallbacks {

    // Holds a reference to the application to manage lifecycle callbacks.
    private lateinit var application: Application

    // Screen recorder instance.
    private lateinit var quashRecorder: QuashRecorder

    // Weak reference to the current activity to prevent memory leaks.
    private var currentActivityReference: WeakReference<Activity?> = WeakReference(null)

    /**
     * Initialize with the application context and a screen recorder.
     *
     * @param app The application instance.
     * @param quashRecorder The screen recorder instance.
     */
    fun initialize(app: Application, quashRecorder: QuashRecorder) {
        application = app
        app.registerActivityLifecycleCallbacks(this)
        this.quashRecorder = quashRecorder
    }

    /**
     * Returns the currently active activity, if available.
     */
    val currentActivity: Activity?
        get() = currentActivityReference.get()

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}

    override fun onActivityResumed(activity: Activity) {
        // Log and start screen recording when activity is resumed
        currentActivityReference = WeakReference(activity)
        if (activity !is QuashBugReportActivity && activity !is QuashCrashNotifierActivity) {
            quashRecorder.startSession()
            handleOverlay(true, activity)  // Manage overlay for non-report activities.
        }
    }

    override fun onActivityPaused(activity: Activity) {
        if (activity !is QuashBugReportActivity) {
            currentActivityReference.clear()
            quashRecorder.stopSession()
            handleOverlay(false, activity)  // Clean up the overlay when the activity pauses.
        }
    }

    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        // Consider adding cleanup logic here if necessary.
    }

    /**
     * Clean up resources on application shutdown or when it is no longer necessary to track activities.
     */
    private fun unregisterActivityCallbacks() {
        currentActivityReference.clear()
        quashRecorder.stopSession()
        quashRecorder.cleanup()
        application.unregisterActivityLifecycleCallbacks(this)
    }

    /**
     * Add or remove an interaction overlay from the activity based on whether recording is active.
     *
     * @param isRecording Whether screen recording is currently active.
     * @param activity The current activity context.
     */
    private fun handleOverlay(isRecording: Boolean, activity: Activity) {
        val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
        val overlayView = rootView.findViewWithTag<QuashInteractionOverlayView>("InteractionOverlay")

        if (isRecording && overlayView == null) {
            rootView.addView(
                QuashInteractionOverlayView(activity).apply {
                tag = "InteractionOverlay"
            }, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
            Log.d("QuashAppLifeCycleCallbacks", "Adding QuashInteractionOverlayView to the activity")
        } else if (!isRecording && overlayView != null) {
            rootView.removeView(overlayView)
            Log.d("QuashAppLifeCycleCallbacks", "Removing QuashInteractionOverlayView from the activity")
        }
    }
}
