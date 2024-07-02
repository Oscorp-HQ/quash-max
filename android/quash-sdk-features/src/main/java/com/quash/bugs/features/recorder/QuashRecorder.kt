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

package com.quash.bugs.features.recorder

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import com.quash.bugs.core.flowcontrol.QuashFlowManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

/**
 * Interface defining the functionality of the QuashRecorder.
 */
interface QuashRecorder {
    fun startSession()
    fun stopSession()
    fun setRecordingDuration(durationSeconds: Int)
    fun setCaptureFrequency(frequency: CaptureFrequency)
    fun setScreenshotQuality(quality: ScreenshotQuality)
    fun captureScreenshot(): Bitmap?
    fun clearRecorder()
    fun cleanup()

    enum class ScreenshotQuality(
        val scaleFactor: Float,
        val format: Bitmap.CompressFormat,
        val quality: Int
    ) {
        //GOOD(0.5f, Bitmap.CompressFormat.JPEG, 50),
        MEDIUM(1.0f, Bitmap.CompressFormat.JPEG, 75),
        //HD(1.0f, Bitmap.CompressFormat.PNG, 100)
    }

    enum class CaptureFrequency(val interval: Long, val bufferSize: Int) {
        //LOW(1000L, 40),
        MEDIUM(500L, 80),
        //HIGH(250L, 160)
    }
}

/**
 * Implementation of the QuashRecorder interface.
 *
 * @param initialQuality Initial screenshot quality setting.
 * @param initialFrequency Initial capture frequency setting.
 * @param initialDurationSeconds Initial recording duration in seconds.
 * @param quashActivityProvider Provides the current activity for capturing screenshots.
 * @param quashFlowManager Manages the starting of services and saving of bitmap files.
 */
class QuashRecorderImpl(
    initialQuality: QuashRecorder.ScreenshotQuality = QuashRecorder.ScreenshotQuality.MEDIUM,
    initialFrequency: QuashRecorder.CaptureFrequency = QuashRecorder.CaptureFrequency.MEDIUM,
    initialDurationSeconds: Int = 40,  // Default duration is set to 40 seconds
    private val quashFlowManager: QuashFlowManager
) : QuashRecorder {

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private val handler = Handler(Looper.getMainLooper())
    private var lastScreenshot: Bitmap? = null

    private var frameRate = initialFrequency.interval
    private var bufferSize = (initialDurationSeconds * 1000 / frameRate).toInt()
    private var screenshotBuffer = QuashCircularBuffer<Bitmap>(bufferSize)
    private var screenshotQuality: QuashRecorder.ScreenshotQuality = initialQuality
    private var recordingDurationSeconds = initialDurationSeconds

    init {
        frameRate = initialFrequency.interval // Set the initial frame rate based on the frequency
    }

    private fun calculateBufferSize(duration: Int, frameRate: Long): Int {
        return (duration * 1000 / frameRate).toInt() // Calculate buffer size based on duration and frame rate
    }

    override fun setScreenshotQuality(quality: QuashRecorder.ScreenshotQuality) {
        synchronized(this) {
            screenshotQuality = quality // Update the screenshot quality
        }
    }

    override fun setCaptureFrequency(frequency: QuashRecorder.CaptureFrequency) {
        synchronized(this) {
            frameRate = frequency.interval // Update the frame rate
            updateBufferSize()  // Update buffer size based on the new frame rate
        }
    }

    override fun setRecordingDuration(durationSeconds: Int) {
        synchronized(this) {
            recordingDurationSeconds = durationSeconds // Update the recording duration
            updateBufferSize()  // Update buffer size based on the new duration
        }
    }

    private fun updateBufferSize() {
        bufferSize = calculateBufferSize(recordingDurationSeconds, frameRate)
        screenshotBuffer =
            QuashCircularBuffer(bufferSize) // Reinitialize the buffer with the new size
    }

    private fun isBitmapDifferent(newBitmap: Bitmap, oldBitmap: Bitmap?): Boolean {
        // Compare new bitmap with the last to check for changes
        if (oldBitmap == null || newBitmap.width != oldBitmap.width || newBitmap.height != oldBitmap.height) {
            return true
        }
        return !newBitmap.sameAs(oldBitmap)
    }

    private val screenshotTask = object : Runnable {
        @SuppressLint("NewApi")
        override fun run() {
            // Capture a screenshot and check if it is different from the last one
            captureScreenshot()?.let { newScreenshot ->
                if (isBitmapDifferent(newScreenshot, lastScreenshot)) {
                    screenshotBuffer.add(newScreenshot) // Add to buffer
                    val filePath =
                        quashFlowManager.saveBitmapToFile(newScreenshot) // Save the screenshot to file
                    sendFilePathToService(filePath) // Send the file path to the service
                    lastScreenshot = newScreenshot
                }
            }
            handler.postDelayed(this, frameRate) // Schedule the next capture
        }
    }

    private fun sendFilePathToService(filePath: String) {
        // Send the file path to the Quash service
        quashFlowManager.startService { intent ->
            intent.action = "com.quash.bugs.action.SAVE_FILE_PATH"
            intent.putExtra("filePath", filePath)
        }
    }

    override fun startSession() {
        handler.post(screenshotTask) // Start the screenshot task
    }

    override fun stopSession() {
        handler.removeCallbacks(screenshotTask) // Stop the screenshot task
    }

    override fun clearRecorder() {
        screenshotBuffer.clear() // Clear the screenshot buffer
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun captureScreenshot(): Bitmap? {
        // Capture a screenshot from the current activity
        val currentActivity = quashFlowManager.getCurrentActivity() ?: return null
        val rootView = currentActivity.window.decorView.rootView
        if (rootView.width <= 0 || rootView.height <= 0) {
            return null
        }

        val bitmap = Bitmap.createBitmap(rootView.width, rootView.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        rootView.draw(canvas)

        // Retrieve and draw tap indicators
        val overlayView =
            rootView.findViewWithTag<QuashInteractionOverlayView>("InteractionOverlay")
        val tapIndicators = overlayView?.getTapIndicators() ?: emptyList()
        drawTapIndicators(canvas, tapIndicators)

        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * screenshotQuality.scaleFactor).toInt(),
            (bitmap.height * screenshotQuality.scaleFactor).toInt(),
            true
        )
    }

    private fun drawTapIndicators(canvas: Canvas, tapIndicators: List<PointF>) {
        val shadowPaint = Paint().apply {
            color = Color.parseColor("#40000000") // Semi-transparent black for shadow
            style = Paint.Style.FILL
            isAntiAlias = true
            setShadowLayer(10f, 0f, 5f, Color.parseColor("#80000000"))
        }

        val outerCirclePaint = Paint().apply {
            color =
                Color.parseColor("#80CCCCCC") // Semi-transparent grey color for the outer circle
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        val innerCirclePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        tapIndicators.forEach { point ->
            canvas.drawCircle(point.x, point.y, 42f, shadowPaint) // Draw shadow
            canvas.drawCircle(point.x, point.y, 40f, outerCirclePaint) // Draw outer circle
            canvas.drawCircle(point.x, point.y, 20f, innerCirclePaint) // Draw inner circle
        }
    }

    override fun cleanup() {
        ioScope.cancel() // Cancel any ongoing operations in the coroutine scope
    }
}
