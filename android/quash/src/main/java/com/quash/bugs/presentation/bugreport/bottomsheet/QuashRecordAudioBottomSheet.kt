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

package com.quash.bugs.presentation.bugreport.bottomsheet

import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quash.bugs.R
import com.quash.bugs.databinding.QuashFragmentRecordAudioBottomSheetBinding
import com.quash.bugs.presentation.helper.onAudioRecordingAdded
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * A BottomSheetDialogFragment for recording audio within the application.
 *
 * @param onAudioRecordingAdded Callback to handle the audio recording once it's finished.
 */
class QuashRecordAudioBottomSheet(
    private val onAudioRecordingAdded: onAudioRecordingAdded
) : BottomSheetDialogFragment() {

    private lateinit var binding: QuashFragmentRecordAudioBottomSheetBinding
    private lateinit var mediaRecorder: MediaRecorder
    private var isRecording = false
    private lateinit var timer: CountDownTimer
    private var mediaUri = ""
    private var currentTempFile: File? = null

    companion object {
        /**
         * Creates a new instance of QuashRecordAudioBottomSheet.
         *
         * @param onAudioAdded Callback to handle the audio recording once it's finished.
         * @return A new instance of QuashRecordAudioBottomSheet.
         */
        fun newInstance(onAudioAdded: (Uri) -> Unit): QuashRecordAudioBottomSheet {
            val args = Bundle()
            return QuashRecordAudioBottomSheet(onAudioAdded).apply { arguments = args }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.QuashBottomSheetDialogStyle)
    }

    private fun createTempFile(extension: String): File {
        val file = File.createTempFile("temp_", ".$extension", context?.cacheDir)
        return file
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = QuashFragmentRecordAudioBottomSheetBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    /**
     * Initializes the UI components and their click listeners.
     */
    private fun initUi() {
        with(binding) {
            ivClose.setOnClickListener { onCloseClicked() }
            ivStart.setOnClickListener { onStartClicked() }
            ivStop.setOnClickListener { onStopClicked() }
        }
    }

    private fun onCloseClicked() {
        if (isRecording) stopRecording()
        dismiss()
    }

    private fun onStartClicked() {
        toggleRecordingUI(true)
        startRecordingAndTimer()
    }

    private fun onStopClicked() {
        toggleRecordingUI(false)
        stopRecording()
    }

    private fun toggleRecordingUI(isRecording: Boolean) {
        binding.ivStart.visibility = if (isRecording) View.GONE else View.VISIBLE
        binding.ivStop.visibility = if (isRecording) View.VISIBLE else View.GONE
    }

    /**
     * Starts the recording and timer.
     */
    private fun startRecordingAndTimer() {
        startRecording()
        startTimer()
    }

    /**
     * Starts the audio recording.
     */
    private fun startRecording() {
        mediaRecorder = createMediaRecorder().apply { start() }
        isRecording = true
    }

    /**
     * Creates and configures a MediaRecorder instance.
     *
     * @return A configured MediaRecorder instance.
     */
    private fun createMediaRecorder(): MediaRecorder {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(requireContext())
        } else {
            MediaRecorder()
        }

        return recorder.apply {
            currentTempFile = createTempFile("mp3")
            mediaUri = currentTempFile!!.absolutePath
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(mediaUri)
            prepare()
        }
    }

    /**
     * Stops the audio recording and releases the MediaRecorder resources.
     */
    private fun stopRecording() {
        mediaRecorder.run {
            stop()
            release()
        }
        isRecording = false
        timer.cancel()
        val audioFileUri = Uri.fromFile(currentTempFile)
        onAudioRecordingAdded(audioFileUri)
        dismiss()
    }

    /**
     * Starts a countdown timer for the recording session.
     */
    private fun startTimer() {
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) = updateTime(millisUntilFinished)
            override fun onFinish() = stopRecording()
        }.start()
    }

    /**
     * Updates the timer display.
     *
     * @param millisUntilFinished The remaining time in milliseconds.
     */
    private fun updateTime(millisUntilFinished: Long) {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
        binding.tvTime.text = String.format("%02d:%02d", minutes, seconds)
    }
}
