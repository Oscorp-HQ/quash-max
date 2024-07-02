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

package com.quash.bugs.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.quash.bugs.Quash
import com.quash.bugs.R
import com.quash.bugs.core.data.local.QuashBitmapDao
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.di.component.QuashRecorderComponent
import com.quash.bugs.features.recorder.QuashRecorder
import com.quash.bugs.presentation.buglist.activities.QuashEditBugActivity
import com.quash.bugs.presentation.bugreport.activity.QuashBugReportActivity
import com.quash.bugs.presentation.helper.QuashMediaOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

/**
 * Service responsible for managing screen recording and floating action buttons for user interaction.
 * This service handles all aspects of initiating and controlling screen capture, both for recording
 * and for taking screenshots, including managing the lifecycle and UI components like notifications
 * and floating action buttons.
 */
class QuashRecorderService : Service(), CoroutineScope {

    private lateinit var quashRecorder: QuashRecorder

    @Inject
    lateinit var recorderFactory: QuashRecorderComponent.Factory

    @Inject
    lateinit var quashBitmapDao: QuashBitmapDao

    private lateinit var job: Job
    private var isRun = false
    private lateinit var fab: FloatingActionButton
    private lateinit var windowManager: WindowManager

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    private var isEdit: Boolean = false

    init {
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(Quash.context)).build()
            .inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        job = Job()
        val notification = createNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            isEdit = intent.getBooleanExtra("isEdit", false)
            (intent.getSerializableExtra("captureType") as? QuashMediaOption)?.let { captureType ->
                showFab(captureType)
            }
        }
        val recorderComponent = recorderFactory.create(
            QuashRecorder.ScreenshotQuality.MEDIUM,
            QuashRecorder.CaptureFrequency.MEDIUM,
            100
        )
        quashRecorder = recorderComponent.getRecorder()

        return START_NOT_STICKY
    }

    /**
     * Displays a floating action button (FAB) on the screen for controlling recording or screenshot capture.
     */
    private fun showFab(captureType: QuashMediaOption) {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        setupFab(captureType)
        val layoutParams = createWindowLayoutParams()
        windowManager.addView(fab, layoutParams)
    }

    /**
     * Configures the floating action button's appearance and functionality based on the capture type.
     */
    private fun setupFab(captureType: QuashMediaOption) {
        fab = FloatingActionButton(ContextThemeWrapper(this, R.style.QuashAppTheme))
        fab.useCompatPadding = true
        fab.apply {
            layoutParams = ViewGroup.LayoutParams(dpToPx(56), dpToPx(56))
            val (colorResId, iconResId) = when (captureType) {
                QuashMediaOption.SCREEN_RECORD -> Pair(
                    R.color.red_600,
                    R.drawable.quash_ic_cam_corder
                )

                QuashMediaOption.SCREENSHOT -> Pair(R.color.blue_600, R.drawable.quash_ic_cam)
                else -> Pair(R.color.red_600, R.drawable.quash_ic_cam_corder)
            }
            backgroundTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    this@QuashRecorderService,
                    colorResId
                )
            )
            setFabIconAndSize(iconResId)
            setOnClickListener { handleFabClick(captureType) }
        }
    }

    /**
     * Handles click events from the floating action button to start/stop recording or take a screenshot.
     */
    private fun handleFabClick(captureType: QuashMediaOption) {
        if (captureType == QuashMediaOption.SCREEN_RECORD) {
            toggleRecording()
        } else if (captureType == QuashMediaOption.SCREENSHOT) {
            takeScreenshotAndStop()
        }
    }

    /**
     * Sets the icon and size of the floating action button.
     */
    private fun setFabIconAndSize(@DrawableRes iconRes: Int) {
        val iconDrawable =
            AppCompatResources.getDrawable(this@QuashRecorderService, iconRes)?.apply {
                setBounds(0, 0, dpToPx(24), dpToPx(24))
                setColorFilter(
                    ContextCompat.getColor(
                        this@QuashRecorderService,
                        android.R.color.white
                    ), PorterDuff.Mode.SRC_IN
                )
            }
        fab.setImageDrawable(iconDrawable)
    }

    /**
     * Converts dp to px based on the device's screen density.
     */
    private fun dpToPx(dp: Int): Int {
        val density = resources.displayMetrics.density
        return (dp * density).toInt()
    }

    /**
     * Takes a screenshot and stops the service, then starts the appropriate activity based on the edit state.
     */
    @SuppressLint("NewApi")
    private fun takeScreenshotAndStop() {
        launch {
            val bitmap = quashRecorder.captureScreenshot()
            quashRecorder.stopSession()
            val imageUri = saveImageToSharedStorage(
                bitmap ?: Bitmap.createBitmap(
                    1,
                    1,
                    Bitmap.Config.ARGB_8888
                )
            )
            val reportIntent = if (isEdit) {
                Intent(this@QuashRecorderService, QuashEditBugActivity::class.java).apply {
                    putExtra("mediaUri", imageUri.toString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(this@QuashRecorderService, QuashBugReportActivity::class.java).apply {
                    putExtra("mediaUri", imageUri.toString())
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            startActivity(reportIntent)
            stopSelf()
        }
    }

    /**
     * Saves the captured screenshot to shared storage and returns its URI.
     */
    private fun saveImageToSharedStorage(bitmap: Bitmap): Uri? {
        return try {
            val tempFile = File.createTempFile(
                "screenshot_",
                "_${System.currentTimeMillis()}.png",
                applicationContext.cacheDir
            )
            FileOutputStream(tempFile).use { fos ->
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100,
                    fos
                )
            }
            Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Creates layout parameters for the floating action button's window.
     */
    private fun createWindowLayoutParams(): WindowManager.LayoutParams {
        return WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            getWindowType(),
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.END
            x = dpToPx(32)
            y = dpToPx(72)
        }
    }

    /**
     * Determines the type of window to use based on the Android version.
     */
    private fun getWindowType(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    }

    /**
     * Toggles the screen recording state, starting or stopping the recording as necessary.
     */
    private fun toggleRecording() {
        if (isRun) {
            isRun = false
            launch { stopRecordingAndOpenReportActivity() }
        } else {
            quashRecorder.clearRecorder()
            isRun = true
            setFabIconAndSize(R.drawable.quash_ic_stop)
        }
    }

    /**
     * Stops the screen recording and opens the report activity.
     */
    private fun stopRecordingAndOpenReportActivity() {
        quashRecorder.stopSession()
        val reportIntent = if (isEdit) {
            Intent(
                this@QuashRecorderService,
                QuashEditBugActivity::class.java
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        } else {
            Intent(this@QuashRecorderService, QuashBugReportActivity::class.java).apply {
                putExtra("isComingFromRecordingService", true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        startActivity(reportIntent)
        stopSelf()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        // If you have a VirtualDisplay, release it here
        job.cancel()
        try {
            windowManager.removeView(fab)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stopForeground(true)
        stopSelf()
    }

    /**
     * Creates a notification for the foreground service.
     * This notification is required to keep the service running in the foreground.
     */
    private fun createNotification(): Notification {
        val importance = NotificationManager.IMPORTANCE_HIGH

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                    setSound(null, null)
                    description = "This is a silent notification channel."
                }
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.quash_ic_quash_notification)
            .setContentTitle("Screen Capture")
            .setContentText("Your screen is being recorded!")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .setSound(null)
            .build()
    }

    companion object {
        private const val NOTIFICATION_ID = 2204
        private const val CHANNEL_ID = "quash_channel_02"
        private const val CHANNEL_NAME = "Quash Recording Channel"
    }
}

