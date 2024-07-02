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
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quash.bugs.core.data.dto.Media
import com.quash.bugs.databinding.QuashItemAudioBinding
import com.quash.bugs.presentation.helper.EditAudioListener

/**
 * Adapter for displaying and editing audio items in a RecyclerView.
 *
 * @param editAudioListener Listener for handling audio item edit events.
 */
class QuashEditAudioAdapter(private val editAudioListener: EditAudioListener) :
    ListAdapter<Media, RecyclerView.ViewHolder>(
        EditAudioComparator
    ) {
    /**
     * Creates a new ViewHolder for an audio item.
     *
     * @param parent The parent ViewGroup.
     * @param viewType The view type of the new View.
     * @return A new EditAudioViewHolder instance.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        EditAudioViewHolder(
            QuashItemAudioBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), editAudioListener
        )

    /**
     * Binds the data to the ViewHolder at the specified position.
     *
     * @param holder The ViewHolder to bind the data to.
     * @param position The position of the item in the list.
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as EditAudioViewHolder
        holder.setData(getItem(position))
    }
}

/**
 * ViewHolder for an audio item in edit mode.
 *
 * @property binding The ViewBinding for the audio item layout.
 * @property listener Listener for handling audio item edit events.
 */
class EditAudioViewHolder(
    private val binding: QuashItemAudioBinding,
    private val listener: EditAudioListener
) :
    RecyclerView.ViewHolder(binding.root) {
    /**
     * Binds the data to the views in the ViewHolder.
     *
     * @param item The audio item to bind.
     */
    fun setData(item: Media) {
        binding.ivCLose.setOnClickListener {
            listener(item)
        }
    }
}