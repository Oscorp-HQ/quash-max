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
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quash.bugs.core.data.dto.Media
import com.quash.bugs.core.util.loadImageWithCoil
import com.quash.bugs.core.util.loadVideoThumbnail
import com.quash.bugs.databinding.QuashItemBugReportBinding
import com.quash.bugs.databinding.QuashItemImageBinding
import com.quash.bugs.databinding.QuashItemVideoBinding
import com.quash.bugs.presentation.helper.ViewMediaListener

/**
 * Adapter for displaying media items in a RecyclerView.
 *
 * @property onAttachmentOpened A callback to handle the opening of media items.
 */
class QuashViewMediaAdapter(private val onAttachmentOpened: ViewMediaListener) :
    ListAdapter<Media, RecyclerView.ViewHolder>(ViewMediaComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ITEM_TYPE_VIDEO -> {
                val binding = QuashItemVideoBinding.inflate(inflater, parent, false)
                VideoViewHolder(binding, onAttachmentOpened)
            }
            ITEM_TYPE_SCREENSHOT -> {
                val binding = QuashItemImageBinding.inflate(inflater, parent, false)
                ImageViewHolder(binding, onAttachmentOpened)
            }
            ITEM_TYPE_BUG_REPORT -> {
                val binding = QuashItemBugReportBinding.inflate(inflater, parent, false)
                BugReportViewHolder(binding)
            }
            else -> throw IllegalArgumentException(INVALID)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is VideoViewHolder -> holder.bind(getItem(position))
            is ImageViewHolder -> holder.bind(getItem(position))
            is BugReportViewHolder -> holder.bind(getItem(position))
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

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).mediaType) {
            VIDEO -> ITEM_TYPE_VIDEO
            IMAGE -> ITEM_TYPE_SCREENSHOT
            else -> ITEM_TYPE_BUG_REPORT
        }
    }
}

/**
 * ViewHolder for displaying video items.
 *
 * @property binding The binding for the video item layout.
 * @property listener A callback to handle the opening of video items.
 */
class VideoViewHolder(
    private val binding: QuashItemVideoBinding,
    private val listener: ViewMediaListener
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Binds a media item to the view.
     *
     * @param media The media item to bind.
     */
    fun bind(media: Media) {
        loadVideoThumbnail(binding.viewVideo, media.mediaUrl)
        binding.ivCLose.visibility = View.GONE
        itemView.setOnClickListener {
            listener.invoke(media.mediaUrl, true)
        }
    }
}

/**
 * ViewHolder for displaying image items.
 *
 * @property binding The binding for the image item layout.
 * @property listener A callback to handle the opening of image items.
 */
class ImageViewHolder(
    private val binding: QuashItemImageBinding,
    private val listener: ViewMediaListener
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Binds a media item to the view.
     *
     * @param media The media item to bind.
     */
    fun bind(media: Media) {
        binding.iv.loadImageWithCoil(media.mediaUrl)
        binding.ivCLose.visibility = View.GONE
        itemView.setOnClickListener {
            listener.invoke(media.mediaUrl, false)
        }
    }
}

/**
 * ViewHolder for displaying bug report items.
 *
 * @property binding The binding for the bug report item layout.
 */
class BugReportViewHolder(private val binding: QuashItemBugReportBinding) :
    RecyclerView.ViewHolder(binding.root) {

    /**
     * Binds a media item to the view.
     *
     * @param media The media item to bind.
     */
    fun bind(media: Media) {
        binding.iv.loadImageWithCoil(media.mediaUrl)
        binding.ivCLose.visibility = View.GONE
    }
}

/**
 * DiffUtil.ItemCallback implementation for Media items.
 */
object ViewMediaComparator : DiffUtil.ItemCallback<Media>() {

    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem.mediaUrl == newItem.mediaUrl
    }

    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem == newItem
    }
}
