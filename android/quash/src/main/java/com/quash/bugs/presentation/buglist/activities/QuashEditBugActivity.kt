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

package com.quash.bugs.presentation.buglist.activities

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.quash.bugs.R
import com.quash.bugs.core.data.dto.Media
import com.quash.bugs.core.data.dto.QuashBugConstant
import com.quash.bugs.core.data.dto.Report
import com.quash.bugs.core.data.dto.ReporterInt
import com.quash.bugs.core.data.preference.QuashCommonPreferences
import com.quash.bugs.core.data.preference.QuashUserPreferences
import com.quash.bugs.core.data.remote.QuashToastHandler
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.core.util.QuashAmplitudeLogger
import com.quash.bugs.core.util.QuashEventConstant
import com.quash.bugs.core.util.asInitials
import com.quash.bugs.core.util.getApiName
import com.quash.bugs.core.util.isFileSizeWithinLimit
import com.quash.bugs.databinding.QuashActivityEditBugBinding
import com.quash.bugs.databinding.QuashDialogCloseBinding
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.di.module.ViewModelFactory
import com.quash.bugs.presentation.buglist.adapters.QuashEditAudioAdapter
import com.quash.bugs.presentation.buglist.adapters.QuashEditMediaAdapter
import com.quash.bugs.presentation.buglist.viewmodel.QuashBugListViewModel
import com.quash.bugs.presentation.bugreport.bottomsheet.QuashAttachmentBottomSheet
import com.quash.bugs.presentation.bugreport.bottomsheet.QuashRecordAudioBottomSheet
import com.quash.bugs.presentation.bugreport.bottomsheet.QuashSelectReporterBottomSheet
import com.quash.bugs.presentation.bugreport.viewmodel.QuashBugReportViewModel
import com.quash.bugs.presentation.common.QuashLoaderView
import com.quash.bugs.presentation.common.QuashMediaDialog
import com.quash.bugs.presentation.common.QuashPriorityBottomSheet
import com.quash.bugs.presentation.common.QuashTypeBottomSheet
import com.quash.bugs.presentation.helper.AfterPermissionGranted
import com.quash.bugs.presentation.helper.QuashMediaOption
import com.quash.bugs.presentation.helper.QuashPermissionUtil
import com.quash.bugs.presentation.helper.QuashPriorityGenerator.priorityList
import com.quash.bugs.presentation.helper.QuashPriorityGenerator.typeList
import com.quash.bugs.service.QuashRecorderService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.Locale
import javax.inject.Inject

/**
 * Activity class for editing bug reports.
 */
class QuashEditBugActivity : AppCompatActivity() {

    private lateinit var binding: QuashActivityEditBugBinding
    private var media = ArrayList<Media>()
    private val audioAdapter = QuashEditAudioAdapter(::onAudioClick)
    private val mediaAdapter = QuashEditMediaAdapter(::onMediaClick, ::onViewMedia)
    private var allAudio: MutableList<Media> = mutableListOf()
    private var allMedia: MutableList<Media> = mutableListOf()
    private var audioUri: MutableList<Uri> = mutableListOf()
    private var mediaUri: MutableList<Uri> = mutableListOf()
    private var removedMediaId: MutableList<String> = mutableListOf()
    private var report: Report? = null
    private var reporterIntList = ArrayList<ReporterInt>()
    private var reporterId = ""
    private var isCrash = false
    private lateinit var quashLoaderView: QuashLoaderView
    private lateinit var toastHandler: QuashToastHandler

    @Inject
    lateinit var amplitudeLogger: QuashAmplitudeLogger

    @Inject
    lateinit var sharedPreferencesUtil: QuashCommonPreferences

    @Inject
    lateinit var quashUserPreferences: QuashUserPreferences

    @Inject
    lateinit var viewModelFactory: ViewModelFactory
    private val viewModel by viewModels<QuashBugListViewModel> { viewModelFactory }
    private val quashBugReportViewModel by viewModels<QuashBugReportViewModel> { viewModelFactory }

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = QuashActivityEditBugBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        quashLoaderView = QuashLoaderView(this)
        toastHandler = QuashToastHandler(this)
        injectDependencies()
        setUpUi()
    }

    /**
     * Set up the UI and observe ViewModel data.
     */
    private fun setUpUi() {
        report = QuashBugConstant.report
        binding.rvAudio.adapter = audioAdapter
        binding.rvMedia.adapter = mediaAdapter
        report?.let {
            reportEvent(EDIT_PAGE_VIEWED)
            with(binding) {
                etTitle.setText(it.title)
                etDesc.setText(it.description)
                tvBugId.text = it.id
                report?.type?.let { type -> setTypeUi(type) }
                report?.priority?.let { prior -> setPriorityUi(prior) }
                quashUserPreferences.setUserId(it.reportedBy.fullName ?: "")
                ivIntials.text = it.reportedBy.fullName?.asInitials()
                tvName.text = it.reportedBy.fullName
                reporterId = it.reportedBy.id ?: BLANK
            }
            report?.listOfMedia?.let { media = it as ArrayList<Media> }
        }
        intent.getStringExtra(MEDIA_URI)?.let {
            restoreSession()
            attachScreenRecord(Uri.parse(it))
        }

        if (report?.type == CRASH) {
            isCrash = true
            binding.tvAdd.isEnabled = false
            binding.tvAdd.isVisible = false
            mediaAdapter.isFromCrashFlow(true)
            binding.tvTypeName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            report?.crashLog2?.let { Media(it.id, it.bugId, it.logUrl, it.id, CRASH) }
                ?.let { media.add(it) }
        }

        processMedia()

        quashBugReportViewModel.requestStateUsers.observe(this) { state ->
            when (state) {
                is RequestState.InProgress -> {
                    // Show loading spinner
                    quashLoaderView.showLoading()
                }

                is RequestState.Successful -> {
                    // Handle success
                    quashLoaderView.hideLoading()
                    reporterIntList.clear()
                    state.data.data.organisationUsers.forEach { user ->
                        val name = user.name ?: UNKNOWN  // Use "Unknown" if name is null
                        reporterIntList.add(ReporterInt(name, user.id))
                    }
                }

                is RequestState.Failed -> {
                    // Show error message
                    quashLoaderView.hideLoading()
                }
            }
        }
        viewModel.requestStateEditBug.observe(this) { state ->
            when (state) {
                is RequestState.InProgress -> {
                    // Show loading spinner
                    quashLoaderView.showLoading()
                }

                is RequestState.Successful -> {
                    // Handle success
                    quashLoaderView.hideLoading()
                    toastHandler.showSuccessToast(REPORT_SUCCESS)
                    reportEvent(EDIT_PAGE_UPDATED)
                    finish()
                }

                is RequestState.Failed -> {
                    // Show error message
                    quashLoaderView.hideLoading()
                    toastHandler.showErrorToast(ERROR)
                }
            }
        }
        setUpOnClick()
    }

    /**
     * Set up click listeners for various UI elements.
     */
    private fun setUpOnClick() {
        with(binding) {
            tvRecord.setOnClickListener {
                val permission = getPermissionsForOption(QuashMediaOption.SCREEN_RECORD)
                if (QuashPermissionUtil.hasPermissions(this@QuashEditBugActivity, *permission)) {
                    showRecordAudioBottomSheet()
                } else {
                    QuashPermissionUtil.requestPermissions(
                        this@QuashEditBugActivity,
                        *permission,
                        requestCode = QuashPermissionUtil.AUDIO_PERMISSION_REQUEST_CODE
                    )
                }
            }
            tvAdd.setOnClickListener { showAttachmentBottomSheet() }
            clReporter.setOnClickListener { showSelectReporterBottomSheet() }
            clPriority.setOnClickListener { showPriorityBottomSheet() }
            tvBugId.setOnClickListener { showCloseDialog() }
            btDiscard.setOnClickListener {
                finish()
            }
            btSave.setOnClickListener {
                updateBug()
            }
            etTitle.setOnClickListener { reportEvent(TITLE_CLICKED) }
            etDesc.setOnClickListener { reportEvent(DESCRIPTION_CLICKED) }
            clType.setOnClickListener {
                if (!isCrash)
                    showTypeBottomSheet()
            }
        }
    }

    /**
     * Show the bottom sheet dialog for recording audio.
     */
    private fun showRecordAudioBottomSheet() {
        reportEvent(ADD_AUDIO_CLICKED)
        QuashRecordAudioBottomSheet.newInstance(::onAudioAdded).show(
            supportFragmentManager, RECORD_AUDIO_FRAG
        )
    }

    /**
     * Show the bottom sheet dialog for adding attachments.
     */
    private fun showAttachmentBottomSheet() {
        reportEvent(ADD_MEDIA_CLICKED)
        QuashAttachmentBottomSheet.createNewInstance(::onAttachmentOptionSelected).show(
            supportFragmentManager, MEDIA_SELECT_FRAG
        )
    }

    /**
     * Handle the attachment option selected from the bottom sheet.
     * @param int The selected option's ID.
     */
    private fun onAttachmentOptionSelected(int: Int) {
        when (val quashMediaOption = QuashMediaOption.fromId(int)) {
            QuashMediaOption.SCREENSHOT -> requestPermissions(quashMediaOption) {
                reportEvent(SCREENSHOT)
                startScreenCaptureScreenShot()
            }

            QuashMediaOption.SCREEN_RECORD -> requestPermissions(quashMediaOption) {
                reportEvent(VIDEO)
                startScreenCapture()
            }

            QuashMediaOption.OPEN_GALLERY -> openGallery()
        }
    }

    /**
     * Show the bottom sheet dialog for selecting priority.
     */
    private fun showPriorityBottomSheet() {
        val fragment = QuashPriorityBottomSheet.newInstance(::onPriorityClick)
        fragment.show(supportFragmentManager, "PriorityBottomSheetFragment")
    }

    /**
     * Show the bottom sheet dialog for selecting bug type.
     */
    private fun showTypeBottomSheet() {
        reportEvent(BUG_TYPE_CLICKED)
        val fragment = QuashTypeBottomSheet.newInstance(::onTypeClick)
        fragment.show(supportFragmentManager, "TypeBottomSheetFragment")
    }

    /**
     * Handle the selected priority from the bottom sheet.
     * @param i The selected priority icon resource ID.
     * @param s The selected priority name.
     */
    private fun onPriorityClick(i: Int, s: String) {
        binding.ivPriority.setImageResource(i)
        binding.tvPriorityName.text = s
    }

    /**
     * Handle the selected bug type from the bottom sheet.
     * @param i The selected type icon resource ID.
     * @param s The selected type name.
     */
    private fun onTypeClick(i: Int, s: String) {
        binding.ivType.setImageResource(i)
        binding.tvTypeName.text = s
    }

    /**
     * Set the priority UI elements based on the provided name.
     * @param name The priority name.
     */
    private fun setPriority(name: String) {
        val list = priorityList().find { it.displayName == name }
        list?.id?.let { binding.ivPriority.setImageResource(it) }
        binding.tvPriorityName.text = list?.displayName
    }

    /**
     * Set the type UI elements based on the provided name.
     * @param name The type name.
     */
    private fun setType(name: String) {
        val list = typeList().find { it.displayName == name }
        list?.id?.let { binding.ivType.setImageResource(it) }
        binding.tvTypeName.text = list?.displayName
    }

    /**
     * Set the priority UI elements based on the server field name.
     * @param name The server field name for the priority.
     */
    private fun setPriorityUi(name: String) {
        val list = priorityList().find { it.serverField == name }
        list?.id?.let { binding.ivPriority.setImageResource(it) }
        binding.tvPriorityName.text = list?.displayName
    }

    /**
     * Set the type UI elements based on the server field name.
     * @param name The server field name for the type.
     */
    private fun setTypeUi(name: String) {
        val list = typeList().find { it.serverField == name }
        list?.id?.let { binding.ivType.setImageResource(it) }
        binding.tvTypeName.text = list?.displayName
    }

    /**
     * Show a dialog to confirm closing the activity without saving.
     */
    private fun showCloseDialog() {
        val binding = QuashDialogCloseBinding.inflate(layoutInflater)
        val dialog =
            MaterialAlertDialogBuilder(
                this,
                R.style.QuashMaterial3AlertDialog
            ).setView(binding.root)
                .setCancelable(false)  // Dialog will not close when clicking outside
                .show()

        binding.apply {
            btDiscard.setOnClickListener {
                dialog.dismiss()
                finish()
            }
            btSave.setOnClickListener {
                dialog.dismiss()
                updateBug()
            }
            ivDialogClose.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    /**
     * Update the bug report with the new details.
     */
    private fun updateBug() {
        viewModel.updateBug(
            report?.id!!,
            binding.etTitle.text.toString(),
            binding.etDesc.text.toString(),
            typeList().getApiName(binding.tvTypeName.text.toString()),
            priorityList().getApiName(binding.tvPriorityName.text.toString()),
            reporterId,
            removedMediaId,
            mediaUri,
            audioUri
        )
        reportEvent()
    }

    /**
     * Handle the added audio recording URI.
     * @param newAudioUri The URI of the new audio recording.
     */
    private fun onAudioAdded(newAudioUri: Uri) {
        // Attempt to add the newly recorded file to the existing list
        allAudio.add(Media(BLANK, BLANK, newAudioUri.toString(), BLANK, AUDIO))
        audioUri.add(newAudioUri)
        audioAdapter.submitList(allAudio)
        binding.rvAudio.adapter = audioAdapter
    }

    /**
     * Inject dependencies required for this activity.
     */
    private fun injectDependencies() {
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(this)).build().inject(this)
    }

    /**
     * Restore the session state from ViewModel.
     */
    private fun restoreSession() {
        report = Gson().fromJson(viewModel.editBug.value.originalMediaJson, Report::class.java)
        val type: Type = object : TypeToken<ArrayList<ReporterInt?>?>() {}.type
        reporterIntList =
            Gson().fromJson(viewModel.editBug.value.user, type) as ArrayList<ReporterInt>

        for (i in reporterIntList) {
            if (i.id == viewModel.editBug.value.edReporterId) {
                binding.ivIntials.text = i.name.asInitials()
                binding.tvName.text = i.name
            }
        }
        mediaUri = viewModel.editBug.value.edMediaFiles.toMutableList()
        audioUri = viewModel.editBug.value.edAudioFiles.toMutableList()
        removedMediaId = viewModel.editBug.value.removedMedia.toMutableList()
        media = report?.listOfMedia as ArrayList<Media>
        setPriority(viewModel.editBug.value.edpriority)
        setType(viewModel.editBug.value.edType)
        with(binding) {
            etTitle.setText(viewModel.editBug.value.edTitle)
            etDesc.setText(viewModel.editBug.value.edDescription)
            tvBugId.text = viewModel.editBug.value.bugId
            reporterId = viewModel.editBug.value.edReporterId
        }
        if (report?.type == CRASH) {
            report?.crashLog2?.let { Media(it.id, it.bugId, it.logUrl, it.id, CRASH) }
                ?.let { media.add(it) }
        }
    }

    /**
     * Save the current session state to the ViewModel.
     */
    private fun saveSession() {
        viewModel.updateBugReport {
            this.bugId = report?.id!!
            this.edTitle = binding.etTitle.text.toString()
            this.edDescription = binding.etDesc.text.toString()
            this.edType = binding.tvTypeName.text.toString()
            this.edMediaFiles = mediaUri
            this.edAudioFiles = audioUri
            this.edReporterId = reporterId
            this.removedMedia = removedMediaId
            this.originalMediaJson = Gson().toJson(report)
            this.user = Gson().toJson(reporterIntList)
            this.edpriority = binding.tvPriorityName.text.toString()
        }
    }

    /**
     * Show the bottom sheet dialog for selecting a reporter.
     */
    private fun showSelectReporterBottomSheet() {
        reportEvent(REPORTER_CLICKED)
        val fragment = QuashSelectReporterBottomSheet.createNewInstance(
            ::onReporterClick,
            binding.tvName.text.toString(),
            reporterIntList
        )
        fragment.show(supportFragmentManager, SELECT_FRAG)
    }

    /**
     * Handle the reporter selected from the bottom sheet.
     * @param name The selected reporter's name.
     * @param id The selected reporter's ID.
     */
    private fun onReporterClick(name: String, id: String) {
        quashUserPreferences.setUserId(name)
        binding.ivIntials.text = name.asInitials()
        binding.tvName.text = name
        reporterId = id
    }

    /**
     * Attach a screen recording URI to the media list.
     * @param videoUri The URI of the screen recording.
     */
    private fun attachScreenRecord(videoUri: Uri) {
        val uriString = videoUri.toString()
        val mediaPAth = if (uriString.startsWith(FILE_PATH)) {
            uriString.substring(7)
        } else {
            uriString
        }
        mediaUri.add(Uri.parse(mediaPAth))
    }

    // Overlay Permission Launchers
    private val overlayPermissionLauncher = registerPermissionLauncher(::startScreenCapture)
    private val overlayPermissionLauncherScreenshot =
        registerPermissionLauncher(::startScreenCaptureScreenShot)

    /**
     * Function to start Screen Capture for Recording
     */
    private fun startScreenCapture() {
        startRecordingService(null)
    }

    /**
     * Function to start Screen Capture for Screenshot
     */
    private fun startScreenCaptureScreenShot() {
        startScreenshotService(null)
    }

    /**
     * Request necessary permissions for the given media option.
     * @param quashMediaOption The media option for which permissions are being requested.
     * @param afterPermissionGranted Lambda to execute after permissions are granted.
     */
    private fun requestPermissions(
        quashMediaOption: QuashMediaOption, afterPermissionGranted: AfterPermissionGranted
    ) {
        val (requestCode, permissionRequester) = when (quashMediaOption) {
            QuashMediaOption.SCREENSHOT -> Pair(
                QuashPermissionUtil.SCREENSHOT_PERMISSION_REQUEST_CODE,
                this::requestPermissionWrapper
            )

            QuashMediaOption.SCREEN_RECORD -> Pair(
                QuashPermissionUtil.SCREEN_RECORD_PERMISSION_REQUEST_CODE,
                this::requestPermissionWrapper
            )

            else -> return
        }

        val permissionsToRequest = getPermissionsForOption(quashMediaOption)

        if (QuashPermissionUtil.hasPermissions(this, *permissionsToRequest)) {
            requestOverlayPermissionAtFirst(afterPermissionGranted, quashMediaOption)
        } else {
            permissionRequester(requestCode, permissionsToRequest)
        }
    }

    /**
     * Create an Intent for the given capture type and data.
     * @param captureType The type of media capture.
     * @param data The data to be included in the Intent.
     * @return The created Intent.
     */
    private fun createIntent(captureType: QuashMediaOption, data: Intent?): Intent {
        return Intent(this, QuashRecorderService::class.java).apply {
            putExtra(TYPE, captureType)
            putExtra(DATA, data)
            putExtra(EDIT, true)
        }
    }

    /**
     * Start the appropriate service for media capture based on Android version.
     * @param intent The Intent to start the service.
     */
    private fun startAppropriateService(intent: Intent) {
        saveSession()
        startService(intent)
    }

    /**
     * Function to handle the start of the Screenshot Service
     * @param data The data to be included in the Intent.
     */
    private fun startScreenshotService(data: Intent?) {
        val intent = createIntent(QuashMediaOption.SCREENSHOT, data)
        startAppropriateService(intent)
        finish()
    }

    /**
     * Function to handle the start of the Recording Service
     * @param data The data to be included in the Intent.
     */
    private fun startRecordingService(data: Intent?) {
        val intent = createIntent(QuashMediaOption.SCREEN_RECORD, data)
        startAppropriateService(intent)
        finish()
    }

    /**
     * Common function to request Overlay Permission.
     * @param overlayPermissionLauncher The launcher to request overlay permission.
     * @param captureMethod The method to execute after permission is granted.
     */
    private fun requestOverlayPermission(
        overlayPermissionLauncher: ActivityResultLauncher<Intent>, captureMethod: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
            )
            overlayPermissionLauncher.launch(intent)
        } else {
            captureMethod()
        }
    }

    /**
     * Register for activity result for overlay permission.
     * @param captureMethod The method to execute after permission is granted.
     * @return The registered ActivityResultLauncher.
     */
    private fun registerPermissionLauncher(captureMethod: () -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    requestOverlayPermission(overlayPermissionLauncher, captureMethod)
                } else {
                    captureMethod()
                }
            }
        }
    }

    /**
     * Get the permissions required for the given media option.
     * @param option The media option.
     * @return An array of required permissions.
     */
    private fun getPermissionsForOption(option: QuashMediaOption): Array<QuashPermissionUtil.PermissionType> {
        val permissions = mutableListOf<QuashPermissionUtil.PermissionType>()
        if (option == QuashMediaOption.SCREEN_RECORD) {
            permissions.add(0, QuashPermissionUtil.PermissionType.RECORD_AUDIO)
        }
        return permissions.toTypedArray()
    }

    /**
     * Wrapper function to request permissions.
     * @param requestCode The request code for the permissions.
     * @param permissions The array of permissions to request.
     */
    private fun requestPermissionWrapper(
        requestCode: Int, permissions: Array<QuashPermissionUtil.PermissionType>
    ) {
        QuashPermissionUtil.requestPermissions(this, *permissions, requestCode = requestCode)
    }

    /**
     * Request overlay permission if necessary, then execute the given lambda.
     * @param afterOverlayPermissionGranted The lambda to execute after permission is granted.
     * @param quashMediaOption The media option.
     */
    private fun requestOverlayPermissionAtFirst(
        afterOverlayPermissionGranted: AfterPermissionGranted, quashMediaOption: QuashMediaOption
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")
                )
                if (quashMediaOption == QuashMediaOption.SCREENSHOT) overlayPermissionLauncherScreenshot.launch(
                    intent
                )
                else if (quashMediaOption == QuashMediaOption.SCREEN_RECORD) overlayPermissionLauncher.launch(
                    intent
                )

            } else {
                afterOverlayPermissionGranted()
            }
        } else {
            afterOverlayPermissionGranted()
        }
    }

    /**
     * Open the gallery for selecting media.
     */
    private fun openGallery() {
        reportEvent(GALLERY)
        val pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/* image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        }
        resultLauncher.launch(pickIntent)
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val receivedUris: List<Uri> = data?.clipData?.let { clipData ->
                    processClipData(clipData)
                } ?: data?.data?.let { uri ->
                    if (contentResolver.isFileSizeWithinLimit(uri)) {
                        listOf(uri)
                    } else {
                        Toast.makeText(
                            this@QuashEditBugActivity,
                            "Cannot upload files more than 15 MB",
                            Toast.LENGTH_SHORT
                        ).show()
                        emptyList()
                    }
                } ?: emptyList()

                handleReceivedMediaUris(receivedUris)
            }
        }

    /**
     * Process the ClipData and extract URIs.
     * @param clipData The ClipData containing multiple URIs.
     * @return A list of URIs.
     */
    private fun processClipData(clipData: ClipData): List<Uri> {
        return List(clipData.itemCount) { i ->
            clipData.getItemAt(i).uri
        }
    }

    /**
     * Handle the received media URIs and update the media list.
     * @param uris The list of received URIs.
     */
    private fun handleReceivedMediaUris(uris: List<Uri>) {
        for (med in uris) {
            allMedia.add(Media(BLANK, BLANK, med.toString(), BLANK, this.getMimeTypeInAllCaps(med)))
            mediaUri.add(med)
        }
        mediaAdapter.submitList(allMedia)
        binding.rvMedia.adapter = mediaAdapter
    }

    /**
     * Handle audio item click event.
     * @param media The clicked media item.
     */
    private fun onAudioClick(media: Media) {
        if (media.id.isNotEmpty())
            removedMediaId.add(media.id)
        else
            audioUri.remove(Uri.parse(media.mediaUrl))
        reportEvent(REMOVE_AUDIO_CLICKED)
        allAudio.remove(media)
        audioAdapter.submitList(allAudio)
        binding.rvAudio.adapter = audioAdapter
    }

    /**
     * Handle media item click event.
     * @param media The clicked media item.
     */
    private fun onMediaClick(media: Media) {
        if (media.id.isNotEmpty())
            removedMediaId.add(media.id)
        else
            mediaUri.remove(Uri.parse(media.mediaUrl))
        reportEvent(REMOVE_MEDIA_CLICKED)
        allMedia.remove(media)
        mediaAdapter.submitList(allMedia)
        binding.rvMedia.adapter = mediaAdapter
    }

    /**
     * Process and update the media list.
     */
    private fun processMedia() {
        val map: MutableSet<String> = mutableSetOf()
        map.addAll(removedMediaId)
        for (i in media) {
            if (i.mediaType == AUDIO && !map.contains(i.id)) {
                allAudio.add(i)
            } else if (!map.contains(i.id)) {
                allMedia.add(i)
            }
        }
        for (audio in audioUri) {
            allAudio.add(Media(BLANK, BLANK, audio.toString(), BLANK, AUDIO))
        }
        for (med in mediaUri) {
            allMedia.add(Media(BLANK, BLANK, med.toString(), BLANK, this.getMimeTypeInAllCaps(med)))
        }
        audioAdapter.submitList(allAudio)
        mediaAdapter.submitList(allMedia)
        binding.rvAudio.adapter = audioAdapter
        binding.rvMedia.adapter = mediaAdapter
    }

    /**
     * Get the MIME type of a URI in uppercase.
     * @param uri The URI to get the MIME type for.
     * @return The MIME type in uppercase.
     */
    private fun Context.getMimeTypeInAllCaps(uri: Uri): String {
        return this.contentResolver.getType(uri)
            ?.substringBefore('/')?.uppercase() ?: get(uri)
    }

    /**
     * Get the MIME type based on the URI extension.
     * @param uri The URI to get the MIME type for.
     * @return The MIME type as a string.
     */
    private fun get(uri: Uri): String {
        val lastPathSegment = uri.lastPathSegment ?: BLANK
        val fileExtension = lastPathSegment.substringAfterLast('.', BLANK)
        return when (fileExtension.lowercase(Locale.ROOT)) {
            MP4, AVI, MKV -> "VIDEO"
            PNG, JPG, JPEG_IMAGE -> "IMAGE"
            GIF -> "IMAGE"
            else -> "FILE"
        }
    }

    /**
     * Log an event for analytics.
     * @param string The event string to log.
     */
    private fun reportEvent(string: String) {
        CoroutineScope(Dispatchers.Main).launch {
            amplitudeLogger.logEvent(string, EDIT_PAGE)
        }
    }

    /**
     * Handle viewing media item.
     * @param s The media URL.
     * @param b Whether the media is a video.
     */
    private fun onViewMedia(s: String, b: Boolean) {
        val quashMediaDialog = QuashMediaDialog.newInstance(s, b)
        quashMediaDialog.show(supportFragmentManager, "MediaDialogFragment")
    }

    /**
     * Log event data for analytics.
     */
    private fun reportEvent() {
        val jsonObject = JSONObject().apply {
            put(QuashEventConstant.Event.SOURCE, EDIT_PAGE)
            put(TITLE_COUNT, binding.etTitle.text?.length)
            put(DESCRIPTION_COUNT, binding.etDesc.text?.length)
            put(BUG_TYPE, typeList().getApiName(binding.tvTypeName.text.toString()))
            put(REPORTER, binding.tvName.text)
            put(AUDIO_RECORDING, if (allAudio.isEmpty()) NO else YES)
            put(MEDIA_COUNT, allMedia.size)
        }
        CoroutineScope(Dispatchers.Main).launch {
            amplitudeLogger.logEvent(REPORT_BUTTON_CLICKED, jsonObject)
        }
    }

    companion object {
        private const val EDIT_PAGE = "Ticket Edit Page"
        private const val EDIT_PAGE_VIEWED = "Ticket Edit Page Viewed"
        private const val EDIT_PAGE_UPDATED = "BE Report Updated"
        private const val REPORTER_CLICKED = "Reporter Drop Down Clicked"
        private const val REPORT_BUTTON_CLICKED = "Report Button Clicked"
        private const val TITLE_CLICKED = "Title Clicked"
        private const val DESCRIPTION_CLICKED = "Description Clicked"
        private const val BUG_TYPE_CLICKED = "Bug Type Clicked"
        private const val ADD_AUDIO_CLICKED = "Audio Add Button Clicked"
        private const val REMOVE_AUDIO_CLICKED = "Audio Remove Button Clicked"
        private const val ADD_MEDIA_CLICKED = "Media Add Button Clicked"
        private const val REMOVE_MEDIA_CLICKED = "Media Remove Button Clicked"
        private const val SCREENSHOT = "Screenshot"
        private const val VIDEO = "Video"
        private const val GALLERY = "Gallery"
        private const val TITLE_COUNT = "Title Count"
        private const val DESCRIPTION_COUNT = "Description Count"
        private const val MEDIA_COUNT = "Media Count"
        private const val AUDIO_RECORDING = "Audio Recording"
        private const val REPORTER = "Reporter"
        private const val BUG_TYPE = "Bug Type"
        private const val BLANK = ""
        private const val NO = "NO"
        private const val YES = "YES"
        private const val CRASH = "CRASH"
        private const val EDIT = "isEdit"
        private const val TYPE = "captureType"
        private const val DATA = "data"
        private const val AUDIO = "AUDIO"
        private const val MEDIA_URI = "mediaUri"
        private const val UNKNOWN = "Unknown"
        private const val FILE_PATH = "file://"
        private const val MP4 = "mp4"
        private const val AVI = "avi"
        private const val MKV = "mkv"
        private const val PNG = "png"
        private const val JPG = "jpg"
        private const val GIF = "gif"
        private const val JPEG_IMAGE = "jpeg"
        private const val REPORT_SUCCESS = "Report Updated Successfully"
        private const val ERROR = "Something Went Wrong! Try Again"
        private const val RECORD_AUDIO_FRAG = "RecordAudioBottomSheetFragment"
        private const val MEDIA_SELECT_FRAG = "AttachmentBottomSheetFragment"
        private const val SELECT_FRAG = "SelectReporterBottomSheetFragment"
    }
}
