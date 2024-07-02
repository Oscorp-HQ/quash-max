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

package com.quash.bugs.core.flowcontrol

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.view.View

/**
 * Provides functionalities to manage services and handle bitmap operations within the Quash application.
 *
 * This interface abstracts the functionality required to start services with specific configurations
 * and to handle the storage of bitmap images.
 */
interface QuashFlowManager {

    /**
     * Starts a service with a custom setup provided via [intentSetup].
     *
     * The method allows passing a lambda that configures the [Intent] according to the specific needs
     * before starting the service. This approach provides flexibility in how services are started.
     *
     * @param intentSetup A lambda function to configure the intent used to start the service.
     */
    fun startService(intentSetup: (Intent) -> Unit)

    /**
     * Saves a bitmap to a file and returns the file path as a string.
     *
     * This method handles the storage of bitmap images to the device's storage, abstracting away
     * the file operations involved in saving and managing the image data.
     *
     * @param bitmap The bitmap image to save.
     * @return The file path where the bitmap was saved.
     */
    fun saveBitmapToFile(bitmap: Bitmap): String

    /**
     * Records an exception in Firebase Crashlytics and sends custom diagnostic data.
     *
     * @param throwable The exception to log.
     * @param threadName The name of the thread where the exception occurred.
     * @param additionalData A map of key-value pairs for additional diagnostic information.
     */
    fun logException(throwable: Throwable, threadName: String?)


    /**
     * Retrieves the current active activity.
     *
     * This method provides a way to access the currently active activity, if available. It can be useful for
     * operations that need to interact with the UI or perform context-specific tasks.
     *
     * @return The current active [Activity], or null if no activity is currently active.
     */
    fun getCurrentActivity(): Activity?

    /**
     * Adds an overlay view to the current activity.
     *
     * This method allows adding an overlay [View] to the current activity's layout. Overlays can be used for
     * displaying additional UI elements on top of the existing activity layout, such as notifications, tooltips,
     * or debugging information.
     *
     * @param overlayView The [View] to be added as an overlay to the current activity.
     */
    fun addOverlayToActivity(overlayView: View)

    /**
     * Removes an overlay view from the current activity.
     *
     * This method allows removing a previously added overlay [View] from the current activity's layout. It ensures
     * that overlays can be dynamically managed and cleaned up as needed to avoid clutter or memory leaks.
     *
     * @param overlayView The [View] to be removed from the current activity's overlay.
     */
    fun removeOverlayFromActivity(overlayView: View)
}
