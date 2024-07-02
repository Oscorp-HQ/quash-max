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

package com.quash.bugs.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.quash.bugs.Quash
import com.quash.bugs.R
import com.quash.bugs.core.data.local.QuashBitmapDao
import com.quash.bugs.core.data.local.QuashBufferItem
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.data.repository.QuashBugReportRepository
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.core.util.QuashBitmapUtils
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.di.component.QuashRecorderComponent
import com.quash.bugs.features.recorder.QuashRecorder
import com.quash.bugs.presentation.buglist.activities.QuashBugListActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Worker class responsible for capturing, saving, and uploading screenshots.
 * This worker is intended to run in the background and manage the lifecycle of screenshot operations.
 *
 * @param appContext The application context
 * @param workerParams The worker parameters
 */
class QuashBitmapSyncWorker(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams) {

    private var quashRecorder: QuashRecorder

    @Inject
    lateinit var quashBitmapDao: QuashBitmapDao

    @Inject
    lateinit var recorderFactory: QuashRecorderComponent.Factory

    @Inject
    lateinit var screenshotRepository: QuashBugReportRepository

    init {
        // Initialize Dagger component
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(appContext)).build()
            .inject(this)

        // Create RecorderComponent with dynamic parameters
        val recorderComponent = recorderFactory.create(
            quality = QuashRecorder.ScreenshotQuality.MEDIUM,
            frequency = QuashRecorder.CaptureFrequency.MEDIUM,
            duration = 40
        )
        quashRecorder = recorderComponent.getRecorder()
    }

    /**
     * The main function to be executed by the worker.
     * Handles capturing, saving, and uploading screenshots.
     *
     * @return The result of the work: either success or failure
     */
    override suspend fun doWork(): Result {
        val reportId = inputData.getString("reportId") ?: return Result.failure()
        Log.i("ScreenshotWorker", "Inside doWork")

        return try {
            val screenshotFilePaths = mutableListOf<String>()
            val startBitmap = createTextBitmap(Quash.context, "Start of the Session", 40f)
            val endBitmap = createTextBitmap(Quash.context, "End of the Session", 40f)
            val startBitmapPath = QuashBitmapUtils.saveBitmapToFile(Quash.context, startBitmap)
            val endBitmapPath = QuashBitmapUtils.saveBitmapToFile(Quash.context, endBitmap)

            val bitmapList = listOf(QuashBufferItem(filePath = startBitmapPath)) +
                    quashBitmapDao.getAllBitmaps() +
                    listOf(QuashBufferItem(filePath = endBitmapPath))

            bitmapList.forEach { bitmap ->
                screenshotFilePaths.add(bitmap.filePath)
                Log.i("ScreenshotWorker", "Inside bitmap conversion loop $screenshotFilePaths")
            }

            val multipartList = screenshotFilePaths.map { filePath ->
                val file = File(filePath)
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                MultipartBody.Part.createFormData("bitmaps", file.name, requestFile)
            }

            val result = submitScreenshots(multipartList, reportId)

            if (result == Result.success()) {
                showNotification()
            }
            return result
        } catch (e: Exception) {
            Log.e("ScreenshotWorker", "Error during bitmap collection: ${e.message}")
            Result.failure()
        }
    }

    /**
     * Submits the captured screenshots to the server.
     *
     * @param multipartList The list of MultipartBody parts representing the screenshots
     * @param reportId The report ID to associate with the screenshots
     * @return The result of the submission: either success or failure
     */
    private suspend fun submitScreenshots(
        multipartList: List<MultipartBody.Part>,
        reportId: String
    ): Result {
        return try {
            val submitResult = screenshotRepository.submitScreenshot(reportId, multipartList)
            submitResult.collect { state ->
                when (state) {
                    is RequestState.Successful -> {
                        Log.i("Report:", "Screenshot published")
                        quashBitmapDao.clearAll()
                        quashRecorder.clearRecorder()
                    }
                    is RequestState.Failed -> {
                        Log.i("Report:", "Screenshot failed")
                        quashBitmapDao.clearAll()
                        quashRecorder.clearRecorder()
                    }
                    else -> {
                        Log.i("Report:", "Data clear invoked")
                        quashBitmapDao.clearAll()
                        quashRecorder.clearRecorder()
                        Log.i("Report:", "Data clear invoked" + quashBitmapDao.getAllBitmaps().size)
                    }
                }
            }
            Log.i("ScreenshotWorker", "Success")
            Result.success()
        } catch (e: Exception) {
            Log.i("ScreenshotWorker", "Failure" + e.message)
            quashBitmapDao.clearAll()
            quashRecorder.clearRecorder()
            Result.failure()
        }
    }

    /**
     * Creates a bitmap with specified text.
     *
     * @param context The context to use for resources
     * @param text The text to draw on the bitmap
     * @param textSize The size of the text
     * @return The created bitmap
     */
    fun createTextBitmap(context: Context, text: String, textSize: Float): Bitmap {
        val (width, height) = getScreenSize(context)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(android.graphics.Color.WHITE)

        val paint = Paint().apply {
            color = android.graphics.Color.BLACK
            this.textSize = textSize
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        val xPos = width / 2f
        val yPos = (height / 2f - (paint.descent() + paint.ascent()) / 2)
        canvas.drawText(text, xPos, yPos, paint)

        return bitmap
    }

    /**
     * Retrieves the screen size in pixels.
     *
     * @param context The context to use for resources
     * @return A pair containing the width and height in pixels
     */
    private fun getScreenSize(context: Context): Pair<Int, Int> {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(displayMetrics)
        return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    /**
     * Shows a notification indicating the session is ready.
     */
    private fun showNotification() {
        val notificationIntent = Intent(appContext, QuashBugListActivity::class.java)
        val notificationManager =
            appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "quash_channel_01"
        val channelName = "Quash"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val pendingIntent = PendingIntent.getActivity(
            appContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val builder = NotificationCompat.Builder(appContext, channelId)
            .setContentIntent(pendingIntent)
            .setContentTitle("🎉 Session Ready! 🎉")
            .setContentText("Woohoo! Your session is now ready to view on the dashboard. 🚀")
            .setSmallIcon(R.drawable.quash_ic_quash_notification)
            .setAutoCancel(true)

        notificationManager.notify(notificationId, builder.build())
    }

    companion object {
        private const val NOTIFICATION_ID = 2205
        private const val CHANNEL_ID = "quash_channel_01"
        private const val CHANNEL_NAME = "Quash Sync Channel"
    }
}
