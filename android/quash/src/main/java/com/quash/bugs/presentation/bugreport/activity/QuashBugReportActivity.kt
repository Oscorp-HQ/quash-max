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

package com.quash.bugs.presentation.bugreport.activity

import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.quash.bugs.R
import com.quash.bugs.core.data.dto.ReportBugResponse
import com.quash.bugs.core.data.dto.ReporterInt
import com.quash.bugs.core.data.local.QuashBitmapDao
import com.quash.bugs.core.data.local.QuashNetworkDao
import com.quash.bugs.core.data.preference.QuashCommonPreferences
import com.quash.bugs.core.data.preference.QuashUserPreferences
import com.quash.bugs.core.data.remote.QuashToastHandler
import com.quash.bugs.core.data.remote.RequestState
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.core.util.QuashAmplitudeLogger
import com.quash.bugs.core.util.QuashEventConstant
import com.quash.bugs.core.util.afterTextChanged
import com.quash.bugs.core.util.asInitials
import com.quash.bugs.core.util.getApiName
import com.quash.bugs.core.util.isFileSizeWithinLimit
import com.quash.bugs.databinding.QuashActivityReportBinding
import com.quash.bugs.databinding.QuashDialogCloseBinding
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.di.module.ViewModelFactory
import com.quash.bugs.presentation.bugreport.adaptor.QuashAudioAdapter
import com.quash.bugs.presentation.bugreport.adaptor.QuashImageVideoAdapter
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
import com.quash.bugs.presentation.helper.QuashPermissionUtil.AUDIO_PERMISSION_REQUEST_CODE
import com.quash.bugs.presentation.helper.QuashPermissionUtil.SCREENSHOT_PERMISSION_REQUEST_CODE
import com.quash.bugs.presentation.helper.QuashPermissionUtil.SCREEN_RECORD_PERMISSION_REQUEST_CODE
import com.quash.bugs.presentation.helper.QuashPriorityGenerator
import com.quash.bugs.service.QuashRecorderService
import com.quash.bugs.worker.QuashBitmapSyncWorker
import com.quash.bugs.worker.QuashNetworkLogWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Locale
import javax.inject.Inject

class QuashBugReportActivity : AppCompatActivity() {


    private lateinit var binding: QuashActivityReportBinding
    private val addedMediaItems: MutableList<Uri> = mutableListOf()
    private val attachmentsAdapter =
        QuashImageVideoAdapter(this, ::onAttachmentRemoved, ::onAttachedItemOpened)


    private val quashAudioAdapter = QuashAudioAdapter(::onAudioRemoved)

    private var reporterIntList = ArrayList<ReporterInt>()

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var amplitudeLogger: QuashAmplitudeLogger

    @Inject
    lateinit var quashNetworkDao: QuashNetworkDao

    @Inject
    lateinit var quashBitmapDao: QuashBitmapDao

    @Inject
    lateinit var sharedPreferencesUtil: QuashCommonPreferences

    @Inject
    lateinit var quashUserPreferences: QuashUserPreferences
    private lateinit var toastHandler: QuashToastHandler
    private lateinit var quashLoaderView: QuashLoaderView
    private var isCrash = false

    private val viewModel by viewModels<QuashBugReportViewModel> { viewModelFactory }
    private var sessionID: String? = null
    private var networkLogPath = ""
    private val VIDEO_PLACEHOLDER_URI = "placeholder://icon"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding = QuashActivityReportBinding.inflate(layoutInflater)
        setContentView(binding.root)
        quashLoaderView = QuashLoaderView(this)
        toastHandler = QuashToastHandler(this)
        injectDependencies()
        setupUI()
        observeNetworkFilePath()
        sessionID = viewModel.sessionID
    }

    private fun observeNetworkFilePath() {
        viewModel.networkFilePathLiveData.observe(this) { filePath ->
            networkLogPath = filePath.quashLogFilePath
        }
    }

    private fun injectDependencies() {
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(this)).build().inject(this)
    }

    private fun onAudioRemoved(uri: Uri) {
        reportEvent(REMOVE_AUDIO_CLICKED)
        val audioUris = listOf<Uri>()
        quashAudioAdapter.submitList(audioUris)
    }


    private fun setupUI() {
        binding.rvAudio.adapter = quashAudioAdapter
        binding.rvMedia.adapter = attachmentsAdapter

        val isComingFromScreenRecordingService =
            intent.getBooleanExtra("isComingFromRecordingService", false)
        if (isComingFromScreenRecordingService) {
            refreshAdapterWithPlaceHolder()
        }

        val uriListString = intent.getStringExtra(URI_LIST)
        try {
            uriListString?.split(",")?.map { Uri.parse(it) }?.let {
                attachLogcatFile(it.toMutableList())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        intent.getStringExtra(MEDIA_URI)?.let {
            restoreSession()
            attachScreenRecord(Uri.parse(it))
        }


        intent.getStringExtra(MEDIA_URI_ON_SHAKE)?.let {
            attachOnShakeMedia(Uri.parse(it))
        }

        if (intent.getStringExtra(URI_LIST).isNullOrEmpty()) {
            reportEvent(REPORT_PAGE_VIEWED, MANUAL)
        } else {
            reportEvent(REPORT_PAGE_VIEWED, AUTOMATIC)
        }

        binding.etTitle.afterTextChanged {
            binding.btnReportBug.isEnabled = it.isNotEmpty()
        }

        viewModel.requestStateBugReport.observe(this) { state ->
            when (state) {
                is RequestState.InProgress -> {
                    // Show loading spinner
                    quashLoaderView.showLoading()
                }

                is RequestState.Successful -> {
                    // Handle success
                    Log.i("Report:", "Successfully Created")
                    sharedPreferencesUtil.setIsCrashReportOngoing(false)
                    quashLoaderView.hideLoading()
                    toastHandler.showSuccessToast(BUG_SUCCESS)
                    reportEvent(REPORT_PAGE_UPDATED)
                    Log.i("Report:", "Report ID" + state.data.data.id)
                    publishNetworkLogs(state)
                    publishScreenshots(state)
                    finish()
                }

                is RequestState.Failed -> {
                    // Show error message
                    quashLoaderView.hideLoading()
                }
            }
        }

        viewModel.requestStateUsers.observe(this) { state ->
            when (state) {
                is RequestState.InProgress -> {
                    // Show loading spinner
                    quashLoaderView.showLoading()
                }

                is RequestState.Successful -> {
                    quashLoaderView.hideLoading()
                    state.data.data.organisationUsers.let { users ->
                        reporterIntList.addAll(users.map { user ->
                            ReporterInt(user.name ?: UNKNOWN, user.id)
                        })
                        users.firstOrNull()?.let { firstUser ->
                            onReporterClick(firstUser.name ?: UNKNOWN, firstUser.id)
                        }
                    }
                }

                is RequestState.Failed -> {
                    // Show error message
                    quashLoaderView.hideLoading()
                }
            }
        }

        setupClickListeners()
    }

    private fun refreshAdapterWithPlaceHolder() {
        if (addedMediaItems.isEmpty()) {
            val placeholderUri = Uri.parse(VIDEO_PLACEHOLDER_URI)
            addedMediaItems += viewModel.bugReport.value.mediaFiles
            addedMediaItems.add(placeholderUri)
            attachmentsAdapter.submitList(addedMediaItems.toList())
        }
    }


    private fun publishScreenshots(state: RequestState.Successful<ReportBugResponse>) {
        Log.i("Report:", "Inside publish Screenshot")
        val reportId = state.data.data.id

        val workData = workDataOf(
            "reportId" to reportId
        )

        val workRequest = OneTimeWorkRequestBuilder<QuashBitmapSyncWorker>()
            .setInputData(workData)
            .build()

        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueue(workRequest)
    }

    private fun publishNetworkLogs(state: RequestState.Successful<ReportBugResponse>) {
        Log.i("Report:", "Inside publish NetworkLogs")
        val reportId = state.data.data.id
        val workData = workDataOf(
            "reportId" to reportId,
            "filePath" to networkLogPath
        )
        val workRequest = OneTimeWorkRequestBuilder<QuashNetworkLogWorker>()
            .setInputData(workData)
            .build()
        WorkManager.getInstance(application).enqueue(workRequest)
    }

    private fun saveSession() {
        viewModel.updateBugReport {
            this.title = binding.etTitle.text.toString()
            this.description = binding.etDesc.text.toString()
            this.priority = binding.tvPriorityName.text.toString()
            this.type = binding.tvTypeName.text.toString()
            this.mediaFiles = addedMediaItems
            this.audioFiles = quashAudioAdapter.currentList
        }
        viewModel.saveData()
    }

    private fun restoreSession() {
        binding.apply {
            etTitle.setText(viewModel.bugReport.value.title)
            etDesc.setText(viewModel.bugReport.value.description)
            setTypeUi(viewModel.bugReport.value.type)
            setPriority(viewModel.bugReport.value.priority)
        }
        quashAudioAdapter.submitList(viewModel.bugReport.value.audioFiles)
    }

    private fun attachScreenRecord(filePath: Uri) {
        addedMediaItems += viewModel.bugReport.value.mediaFiles
        val uriString = filePath.toString()
        val mediaPAth = if (uriString.startsWith(FILE_PATH)) {
            uriString.substring(7)
        } else {
            uriString
        }

        addedMediaItems.add(Uri.parse(mediaPAth))
        attachmentsAdapter.submitList(addedMediaItems)
        viewModel.updateBugReport {
            this.mediaFiles += addedMediaItems
        }
        binding.btnReportBug.isEnabled = true
    }

    private fun attachOnShakeMedia(filePath: Uri) {
        addedMediaItems.add(filePath)
        attachmentsAdapter.submitList(addedMediaItems)
        viewModel.updateBugReport {
            this.mediaFiles += addedMediaItems
        }
        binding.btnReportBug.isEnabled = true
    }

    private fun attachLogcatFile(crashFiles: MutableList<Uri>) {
        setTypeUi("Crash")
        binding.tvAdd.isEnabled = false
        binding.tvAdd.isVisible = false
        binding.tvTypeName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
        // Create a new list combining the current media files and the new logcatUri
        attachmentsAdapter.isFromCrashFlow(true)
        isCrash = true
        val placeholderUri = Uri.parse(VIDEO_PLACEHOLDER_URI)
        crashFiles.add(placeholderUri)
        attachmentsAdapter.submitList(crashFiles)
        viewModel.updateBugReport {
            this.crashLog = crashFiles.first()
            this.type = binding.tvTypeName.text.toString()
        }
    }


    private fun setupClickListeners() {
        with(binding) {
            tvRecord.setOnClickListener {
                val permission = getPermissionsForOption(QuashMediaOption.SCREEN_RECORD)
                if (QuashPermissionUtil.hasPermissions(this@QuashBugReportActivity, *permission)) {
                    showRecordAudioBottomSheet()

                } else {
                    QuashPermissionUtil.requestPermissions(
                        this@QuashBugReportActivity,
                        *permission,
                        requestCode = AUDIO_PERMISSION_REQUEST_CODE
                    )
                }
            }
            tvAdd.setOnClickListener { showAttachmentBottomSheet() }
            clReporter.setOnClickListener { showSelectReporterBottomSheet() }
            ivClose.setOnClickListener { showCloseDialog() }
            clPriority.setOnClickListener { showPriorityBottomSheet() }
            clType.setOnClickListener {
                if (!isCrash)
                    showTypeBottomSheet()
            }
            btnReportBug.setOnClickListener {
                viewModel.updateBugReport {
                    this.title = binding.etTitle.text.toString()
                    this.description = binding.etDesc.text.toString()
                    this.priority =
                        QuashPriorityGenerator.priorityList()
                            .getApiName(binding.tvPriorityName.text.toString())
                    this.type = viewModel.transformFields(binding.tvTypeName.text.toString())
                }

                /*lifecycleScope.launch(Dispatchers.IO) {
                    Log.d(
                        "QuashRecorder", "Before sending to BE size- ${
                            quashBitmapDao.getAllBitmaps().size
                        }"
                    )
                }*/

                viewModel.reportBug()
                reportEvent()
            }

            etTitle.setOnClickListener { reportEvent(TITLE_CLICKED) }
            etDesc.setOnClickListener { reportEvent(DESCRIPTION_CLICKED) }
        }
    }

    private fun showSelectReporterBottomSheet() {
        reportEvent(REPORTER_CLICKED)
        val fragment = QuashSelectReporterBottomSheet.createNewInstance(
            ::onReporterClick,
            binding.tvName.text.toString(),
            reporterIntList
        )
        fragment.show(supportFragmentManager, SELECT_FRAG)
    }

    private fun showPriorityBottomSheet() {
        val fragment = QuashPriorityBottomSheet.newInstance(::onPriorityClick)
        fragment.show(supportFragmentManager, "PriorityBottomSheetFragment")
    }

    private fun showTypeBottomSheet() {
        reportEvent(BUG_TYPE_CLICKED)
        val fragment = QuashTypeBottomSheet.newInstance(::onTypeClick)
        fragment.show(supportFragmentManager, "TypeBottomSheetFragment")
    }

    private fun setPriority(name: String) {
        val list = QuashPriorityGenerator.priorityList().find { it.displayName == name }
        list?.id?.let { binding.ivPriority.setImageResource(it) }
        binding.tvPriorityName.text = list?.displayName
    }

    private fun setTypeUi(name: String) {
        val list = QuashPriorityGenerator.typeList().find { it.displayName == name }
        list?.id?.let { binding.ivType.setImageResource(it) }
        binding.tvTypeName.text = list?.displayName
    }

    private fun onPriorityClick(i: Int, s: String) {
        binding.ivPriority.setImageResource(i)
        binding.tvPriorityName.text = s
        viewModel.updateBugReport {
            this.priority = s
        }
    }

    private fun onTypeClick(i: Int, s: String) {
        binding.ivType.setImageResource(i)
        binding.tvTypeName.text = s
        viewModel.updateBugReport { type = s }
    }

    private fun onReporterClick(name: String, id: String) {
        quashUserPreferences.setUserId(name)
        binding.ivIntials.text = name.asInitials()
        binding.tvName.text = name
        viewModel.updateBugReport { reporterId = id }
    }

    private fun showRecordAudioBottomSheet() {
        reportEvent(ADD_AUDIO_CLICKED)
        QuashRecordAudioBottomSheet.newInstance(::onAudioAdded).show(
            supportFragmentManager, RECORD_AUDIO_FRAG
        )
    }

    private fun onAudioAdded(newAudioUri: Uri) {
        val audioUris = listOf(newAudioUri)
        quashAudioAdapter.submitList(audioUris)
        viewModel.updateBugReport { this.audioFiles = audioUris }
    }


    private fun showAttachmentBottomSheet() {
        reportEvent(ADD_MEDIA_CLICKED)
        QuashAttachmentBottomSheet.createNewInstance(::onAttachmentOptionSelected).show(
            supportFragmentManager, MEDIA_SELECT_FRAG
        )
    }

    private fun onAttachmentRemoved(int: Int, type: String) {
        reportEvent(REMOVE_MEDIA_CLICKED)
        if (type == "VIDEO") {
            viewModel.clearGifs()
        }
        addedMediaItems.removeAt(int)
        binding.rvMedia.adapter = attachmentsAdapter
        attachmentsAdapter.submitList(addedMediaItems)
        viewModel.updateBugReport {
            this.mediaFiles = addedMediaItems
        }
    }

    //handling screen capture starts
    private fun onAttachmentOptionSelected(int: Int) {
        when (val quashMediaOption = QuashMediaOption.fromId(int)) {
            QuashMediaOption.SCREENSHOT -> requestPermissions(quashMediaOption)
            {
                reportEvent(SCREENSHOT)
                startScreenCaptureScreenShot()
            }

            QuashMediaOption.SCREEN_RECORD -> requestPermissions(quashMediaOption)
            {
                reportEvent(VIDEO)
                startScreenCapture()
            }

            QuashMediaOption.OPEN_GALLERY -> openGallery()
        }
    }

    // Function to start Screen Capture for Recording
    private fun startScreenCapture() {
        startRecordingService(null)
    }

    // Function to start Screen Capture for Screenshot
    private fun startScreenCaptureScreenShot() {
        startScreenshotService(null)
    }

    private fun requestPermissions(
        quashMediaOption: QuashMediaOption, afterPermissionGranted: AfterPermissionGranted
    ) {
        val (requestCode, permissionRequester) = when (quashMediaOption) {
            QuashMediaOption.SCREENSHOT -> Pair(
                SCREENSHOT_PERMISSION_REQUEST_CODE, this::requestPermissionWrapper
            )

            QuashMediaOption.SCREEN_RECORD -> Pair(
                SCREEN_RECORD_PERMISSION_REQUEST_CODE, this::requestPermissionWrapper
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

    private fun getPermissionsForOption(option: QuashMediaOption): Array<QuashPermissionUtil.PermissionType> {
        val permissions = mutableListOf<QuashPermissionUtil.PermissionType>()
        if (option == QuashMediaOption.SCREEN_RECORD) {
            permissions.add(0, QuashPermissionUtil.PermissionType.RECORD_AUDIO)
        }
        return permissions.toTypedArray()
    }

    private fun requestPermissionWrapper(
        requestCode: Int, permissions: Array<QuashPermissionUtil.PermissionType>
    ) {
        QuashPermissionUtil.requestPermissions(this, *permissions, requestCode = requestCode)
    }

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

    private fun createIntent(captureType: QuashMediaOption, data: Intent?): Intent {
        return Intent(this, QuashRecorderService::class.java).apply {
            putExtra(TYPE, captureType)
            putExtra(DATA, data)
        }
    }

    // Function to start Foreground or Regular Service based on Android Version
    private fun startAppropriateService(intent: Intent) {
        saveSession()
        startService(intent)
    }

    // Function to handle the start of the Screenshot Service
    private fun startScreenshotService(data: Intent?) {
        val intent = createIntent(QuashMediaOption.SCREENSHOT, data)
        startAppropriateService(intent)
        finish()
    }

    // Function to handle the start of the Recording Service
    private fun startRecordingService(data: Intent?) {
        val intent = createIntent(QuashMediaOption.SCREEN_RECORD, data)
        startAppropriateService(intent)
        finish()
    }

    // Common function to request Overlay Permission
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

    // Register For Activity Result for Overlay Permission
    private fun registerPermissionLauncher(captureMethod: () -> Unit): ActivityResultLauncher<Intent> {
        return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    requestOverlayPermission(overlayPermissionLauncher, captureMethod)
                } else {
                    captureMethod()
                }
            }
        }
    }

    // Overlay Permission Launchers
    private val overlayPermissionLauncher = registerPermissionLauncher(::startScreenCapture)
    private val overlayPermissionLauncherScreenshot =
        registerPermissionLauncher(::startScreenCaptureScreenShot)

    // Permission Actions Mapping
    private val permissionActions: Map<Int, () -> Unit> =
        mapOf(SCREEN_RECORD_PERMISSION_REQUEST_CODE to {
            requestOverlayPermission(
                overlayPermissionLauncher, ::startScreenCapture
            )
        }, SCREENSHOT_PERMISSION_REQUEST_CODE to {
            requestOverlayPermission(
                overlayPermissionLauncherScreenshot, ::startScreenCaptureScreenShot
            )
        }, AUDIO_PERMISSION_REQUEST_CODE to { showRecordAudioBottomSheet() })


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Check if all permissions are granted
        val allGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        if (allGranted) {
            // Perform action related to the request code
            permissionActions[requestCode]?.invoke()
            return
        }

        // Handle denied permissions
        val permanentlyDenied = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            permissions.any { shouldShowRequestPermissionRationale(it).not() }
        } else {
            false // Below API 23, permissions are automatically granted
        }

        QuashPermissionUtil.handlePermissionResult(
            this, requestCode, permanentlyDenied, permissions
        )
    }

    //handling screen capture ends
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

    private fun processClipData(clipData: ClipData): List<Uri> {
        return List(clipData.itemCount) { i ->
            clipData.getItemAt(i).uri
        }
    }

    private fun handleReceivedMediaUris(uris: List<Uri>) {
        addedMediaItems += uris
        binding.rvMedia.adapter = attachmentsAdapter
        attachmentsAdapter.submitList(addedMediaItems)
        viewModel.updateBugReport {
            this.mediaFiles += addedMediaItems
        }
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
                            this@QuashBugReportActivity,
                            "Cannot upload files more than 15 MB",
                            Toast.LENGTH_SHORT
                        ).show()
                        emptyList()
                    }
                } ?: emptyList()

                handleReceivedMediaUris(receivedUris)
            }
        }

    private fun showCloseDialog() {
        reportEvent(TICKET_CLOSE)
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
            }
            btSave.setOnClickListener {
                dialog.dismiss()
                sharedPreferencesUtil.setIsCrashReportOngoing(false)
                finish()
            }
            ivDialogClose.setOnClickListener {
                dialog.dismiss()
            }
        }
    }

    private fun reportEvent(string: String) {
        CoroutineScope(Dispatchers.Main).launch {
            amplitudeLogger.logEvent(string, REPORT_PAGE)
        }
    }

    private fun reportEvent() {
        val jsonObject = JSONObject()
        jsonObject.put(QuashEventConstant.Event.SOURCE, REPORT_PAGE)
        jsonObject.put(TITLE_COUNT, binding.etTitle.text?.length)
        jsonObject.put(DESCRIPTION_COUNT, binding.etDesc.text?.length)
        jsonObject.put(
            BUG_TYPE,
            QuashPriorityGenerator.typeList().getApiName(binding.tvTypeName.text.toString())
        )
        jsonObject.put(REPORTER, binding.tvName.text)
        jsonObject.put(AUDIO_RECORDING, if (quashAudioAdapter.currentList.isEmpty()) NO else YES)
        jsonObject.put(MEDIA_COUNT, addedMediaItems.size)
        CoroutineScope(Dispatchers.Main).launch {
            amplitudeLogger.logEvent(REPORT_BUTTON_CLICKED, jsonObject)
        }
    }

    private fun reportEvent(eventName: String, source: String) {
        CoroutineScope(Dispatchers.Main).launch {
            amplitudeLogger.logEvent(eventName, source)
        }
    }

    companion object {
        private const val REPORT_PAGE = "Ticket Create Page"
        private const val REPORT_PAGE_VIEWED = "Ticket Create Page Viewed"
        private const val REPORT_PAGE_UPDATED = "BE Report Upload Successful"
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
        private const val AUTOMATIC = "Automatic"
        private const val MANUAL = "MANUAL"
        private const val TICKET_CLOSE = "Ticket Create Page Close Button Clicked"
        private const val URI_LIST = "uriList"
        private const val MEDIA_URI = "mediaUri"
        private const val MEDIA_URI_ON_SHAKE = "mediaUriShake"
        private const val TYPE = "captureType"
        private const val DATA = "data"
        private const val NO = "NO"
        private const val YES = "YES"
        private const val BUG_SUCCESS = "Bug Successfully Created"
        private const val UNKNOWN = "Unknown"
        private const val FILE_PATH = "file://"
        private const val RECORD_AUDIO_FRAG = "RecordAudioBottomSheetFragment"
        private const val MEDIA_SELECT_FRAG = "AttachmentBottomSheetFragment"
        private const val SELECT_FRAG = "SelectReporterBottomSheetFragment"
        private const val MEDIA_DIALOG_FRAG = "MediaDialogFragment"
        private const val HTTP = "http"
        private const val HTTP_S = "https"
        private const val MP4 = "mp4"
        private const val AVI = "avi"
        private const val MKV = "mkv"
    }

    private fun onAttachedItemOpened(uri: Uri) {
        val isVideo: Boolean
        val placeholderUriString = VIDEO_PLACEHOLDER_URI
        if (uri.toString() == placeholderUriString) {
            Toast.makeText(
                this,
                "Session Replay is only available on dashboard.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        when (uri.scheme) {
            "content" -> {
                val mimeType = this.contentResolver.getType(uri)
                isVideo = mimeType?.startsWith("video") == true
            }

            in listOf(HTTP, HTTP_S) -> {
                isVideo =
                    uri.toString().substringAfterLast('.').lowercase(Locale.ROOT) in listOf(
                        MP4,
                        AVI,
                        MKV
                    )
            }

            else -> {
                isVideo =
                    uri.toString().substringAfterLast('.').lowercase(Locale.ROOT) in listOf(
                        MP4,
                        AVI,
                        MKV
                    )
            }
        }

        val quashMediaDialog = QuashMediaDialog.newInstance(uri.toString(), isVideo)
        quashMediaDialog.show(supportFragmentManager, MEDIA_DIALOG_FRAG)
    }
}
