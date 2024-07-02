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

import androidx.recyclerview.widget.DiffUtil
import com.quash.bugs.core.data.dto.Media
import com.quash.bugs.core.data.dto.Report

/**
 * Comparator for media items.
 */
object EditMediaComparator : DiffUtil.ItemCallback<Media>() {
    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem.mediaUrl == newItem.mediaUrl
    }

    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem == newItem
    }
}


/**
 * Comparator for audio items in the list.
 */
object ViewAudioComparator : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}

/**
 * Comparator for diffing bug reports.
 */
object BugsListComparator : DiffUtil.ItemCallback<Report>() {
    override fun areItemsTheSame(oldItem: Report, newItem: Report): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Report, newItem: Report): Boolean {
        return oldItem == newItem
    }
}

/**
 * Comparator for audio items in the list.
 */
object EditAudioComparator : DiffUtil.ItemCallback<Media>() {
    /**
     * Determines if two items represent the same object.
     *
     * @param oldItem The old item.
     * @param newItem The new item.
     * @return True if the items represent the same object, false otherwise.
     */
    override fun areItemsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem.mediaUrl == newItem.mediaUrl
    }

    /**
     * Determines if the contents of two items are the same.
     *
     * @param oldItem The old item.
     * @param newItem The new item.
     * @return True if the contents of the items are the same, false otherwise.
     */
    override fun areContentsTheSame(oldItem: Media, newItem: Media): Boolean {
        return oldItem == newItem
    }
}