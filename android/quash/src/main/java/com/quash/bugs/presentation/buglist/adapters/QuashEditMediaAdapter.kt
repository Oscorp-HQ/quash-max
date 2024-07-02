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

package com.quash.bugs.presentation.buglist.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quash.bugs.core.data.dto.Media
import com.quash.bugs.core.util.loadImageWithCoil
import com.quash.bugs.core.util.loadVideoThumbnail
import com.quash.bugs.databinding.QuashItemBugReportBinding
import com.quash.bugs.databinding.QuashItemImageBinding
import com.quash.bugs.databinding.QuashItemVideoBinding
import com.quash.bugs.presentation.helper.EditAudioListener
import com.quash.bugs.presentation.helper.ViewMediaListener

/**
 * Adapter to handle the display and interactions of media items in the bug list.
 *
 * @param editAudioListener Listener for editing audio items.
 * @param viewMedia Listener for viewing media items.
 */
class QuashEditMediaAdapter(
    private val editAudioListener: EditAudioListener,
    private val viewMedia: ViewMediaListener
) : ListAdapter<Media, RecyclerView.ViewHolder>(EditMediaComparator) {

    private var isFromCrashFlow = false

    /**
     * Sets whether the media items are from a crash flow.
     *
     * @param tf True if from crash flow, false otherwise.
     */
    fun isFromCrashFlow(tf: Boolean = false) {
        isFromCrashFlow = tf
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_VIDEO -> {
                val binding = QuashItemVideoBinding.inflate(inflater, parent, false)
                EditVideoViewHolder(binding, editAudioListener, viewMedia)
            }
            ITEM_TYPE_SCREENSHOT -> {
                val binding = QuashItemImageBinding.inflate(inflater, parent, false)
                EditImageViewHolder(binding, editAudioListener, viewMedia, isFromCrashFlow)
            }
            ITEM_TYPE_BUG_REPORT -> {
                val binding = QuashItemBugReportBinding.inflate(inflater, parent, false)
                EditBugReportViewHolder(binding, editAudioListener)
            }
            else -> throw IllegalArgumentException(INVALID)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EditVideoViewHolder -> holder.bind(getItem(position))
            is EditImageViewHolder -> holder.bind(getItem(position))
            is EditBugReportViewHolder -> holder.bind(getItem(position))
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).mediaType) {
            VIDEO -> ITEM_TYPE_VIDEO
            IMAGE -> ITEM_TYPE_SCREENSHOT
            else -> ITEM_TYPE_BUG_REPORT
        }
    }

    companion object {
        const val ITEM_TYPE_SCREENSHOT = 1
        const val ITEM_TYPE_VIDEO = 2
        const val ITEM_TYPE_BUG_REPORT = 3
        const val INVALID = "Invalid view type"
        const val VIDEO = "VIDEO"
        const val IMAGE = "IMAGE"
    }
}

/**
 * ViewHolder for displaying video items.
 *
 * @param binding The binding for the video item view.
 * @param listener The listener for editing audio items.
 * @param viewListener The listener for viewing media items.
 */
class EditVideoViewHolder(
    private val binding: QuashItemVideoBinding,
    private val listener: EditAudioListener,
    private val viewListener: ViewMediaListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(media: Media) {
        if (media.id.isEmpty())
            binding.viewVideo.loadImageWithCoil(media.mediaUrl)
        else
            loadVideoThumbnail(binding.viewVideo, media.mediaUrl)
        binding.ivCLose.setOnClickListener {
            listener(media)
        }
        itemView.setOnClickListener {
            viewListener(media.mediaUrl, true)
        }
    }
}

/**
 * ViewHolder for displaying image items.
 *
 * @param binding The binding for the image item view.
 * @param listener The listener for editing audio items.
 * @param viewListener The listener for viewing media items.
 * @param isCrash Indicates if the media is from a crash flow.
 */
class EditImageViewHolder(
    private val binding: QuashItemImageBinding,
    private val listener: EditAudioListener,
    private val viewListener: ViewMediaListener,
    private val isCrash: Boolean
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(media: Media) {
        binding.iv.loadImageWithCoil(media.mediaUrl)
        if (isCrash)
            binding.ivCLose.visibility = View.GONE
        binding.ivCLose.setOnClickListener {
            listener(media)
        }
        itemView.setOnClickListener {
            viewListener(media.mediaUrl, false)
        }
    }
}

/**
 * ViewHolder for displaying bug report items.
 *
 * @param binding The binding for the bug report item view.
 * @param listener The listener for editing audio items.
 */
class EditBugReportViewHolder(
    private val binding: QuashItemBugReportBinding,
    private val listener: EditAudioListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(media: Media) {
        binding.iv.loadImageWithCoil(media.mediaUrl)
        binding.ivCLose.visibility = View.GONE
    }
}

