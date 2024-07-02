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

package com.quash.bugs.presentation.buglist.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.quash.bugs.R
import com.quash.bugs.core.data.dto.Media
import com.quash.bugs.core.data.dto.QuashBugConstant
import com.quash.bugs.core.data.dto.Report
import com.quash.bugs.core.di.module.QuashCoreModule
import com.quash.bugs.core.util.QuashAmplitudeLogger
import com.quash.bugs.core.util.asInitials
import com.quash.bugs.databinding.QuashFragmentDetailDailogBinding
import com.quash.bugs.di.component.DaggerQuashComponent
import com.quash.bugs.presentation.buglist.activities.QuashEditBugActivity
import com.quash.bugs.presentation.buglist.adapters.QuashViewAudioAdapter
import com.quash.bugs.presentation.buglist.adapters.QuashViewMediaAdapter
import com.quash.bugs.presentation.common.QuashMediaDialog
import com.quash.bugs.presentation.helper.QuashPriorityGenerator.priorityList
import com.quash.bugs.presentation.helper.QuashPriorityGenerator.typeList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Dialog fragment that displays the details of a bug report.
 */
class QuashDetailDialogFragment : DialogFragment() {

    private var report: Report? = null
    private lateinit var binding: QuashFragmentDetailDailogBinding

    @Inject
    lateinit var amplitudeLogger: QuashAmplitudeLogger

    companion object {
        private const val TICKET_VIEW_PAGE = "Ticket View Page"
        private const val TICKET_VIEW_PAGE_VIEWED = "Ticket View Page Viewed"
        private const val EDIT_CLICKED = "Edit Button Clicked"
        private const val MEDIA_DIALOG = "MediaDialogFragment"
        private const val CRASH = "CRASH"
        private const val AUDIO = "AUDIO"
        fun createNewInstance() = QuashDetailDialogFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        report = QuashBugConstant.report
    }

    override fun getTheme(): Int = R.style.QuashDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = QuashFragmentDetailDailogBinding.inflate(layoutInflater, container, false)
        injectDependencies()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        reportEvent(TICKET_VIEW_PAGE_VIEWED)
        if (!report?.listOfMedia.isNullOrEmpty()) {
            setupAudioAdapter()
            setupMediaAdapter()
        }
        binding.btnEditBug.setOnClickListener {
            val intent = Intent(requireContext(), QuashEditBugActivity::class.java)
            startActivity(intent)
            reportEvent(EDIT_CLICKED)
            dialog?.dismiss()
        }

        with(binding) {
            ivIntials.text = report?.reportedBy?.fullName?.asInitials()
            tvName.text = report?.reportedBy?.fullName
            tvBugId.text = report?.id
            etTitle.text = report?.title
            etDesc.text = report?.description
            report?.type?.let { setType(it) }
            report?.priority?.let { setPriority(it) }
        }
        binding.tvBugId.setOnClickListener { dismiss() }
    }

    /**
     * Sets up the adapter for displaying audio media.
     */
    private fun setupAudioAdapter() {
        val list = report?.listOfMedia
            ?.filter { it.mediaType == AUDIO }
            ?.map { it.mediaUrl }
            ?: emptyList()
        val audioAdapter = QuashViewAudioAdapter()
        audioAdapter.submitList(list)
        binding.rvAudio.adapter = audioAdapter
    }

    /**
     * Sets the priority display based on the provided priority name.
     *
     * @param name The priority name.
     */
    private fun setPriority(name: String) {
        val list = priorityList().find { it.serverField == name }
        list?.id?.let {
            binding.tvPriorityDetailType.setCompoundDrawablesWithIntrinsicBounds(
                it,
                0,
                0,
                0
            )
        }
        binding.tvPriorityDetailType.text = list?.displayName
    }

    /**
     * Sets the bug type display based on the provided type name.
     *
     * @param name The bug type name.
     */
    private fun setType(name: String) {
        val list = typeList().find { it.serverField == name }
        list?.id?.let { binding.tvBugType.setCompoundDrawablesWithIntrinsicBounds(it, 0, 0, 0) }
        binding.tvBugType.text = list?.displayName
    }

    /**
     * Sets up the adapter for displaying non-audio media.
     */
    private fun setupMediaAdapter() {
        val media = report?.listOfMedia?.filter { it.mediaType != AUDIO } as ArrayList<Media>
        if (report?.type == CRASH)
            report?.crashLog2?.let { Media(it.id, it.bugId, it.logUrl, it.id, CRASH) }
                ?.let { media.add(it) }

        val mediaAdapter = QuashViewMediaAdapter(::onViewMedia)
        mediaAdapter.submitList(media)
        binding.rvMedia.adapter = mediaAdapter
    }

    /**
     * Callback function for viewing media.
     *
     * @param s The media URL.
     * @param b Whether the media is an image or not.
     */
    private fun onViewMedia(s: String, b: Boolean) {
        val quashMediaDialog = QuashMediaDialog.newInstance(s, b)
        activity?.let { quashMediaDialog.show(it.supportFragmentManager, MEDIA_DIALOG) }
    }

    /**
     * Injects dependencies using Dagger.
     */
    private fun injectDependencies() {
        DaggerQuashComponent.builder().quashCoreModule(QuashCoreModule(requireContext())).build()
            .inject(this)
    }

    /**
     * Reports an event using the QuashAmplitudeLogger.
     *
     * @param string The event name.
     */
    private fun reportEvent(string: String) {
        CoroutineScope(Dispatchers.Main).launch {
            amplitudeLogger.logEvent(string, TICKET_VIEW_PAGE)
        }
    }
}