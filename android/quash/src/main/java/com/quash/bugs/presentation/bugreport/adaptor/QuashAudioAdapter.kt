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

package com.quash.bugs.presentation.bugreport.adaptor

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quash.bugs.databinding.QuashItemAudioBinding
import com.quash.bugs.presentation.helper.OnAudioCloseListener
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Adapter for displaying a list of audio files in a RecyclerView.
 *
 * @property onAudioCloseListener Listener for handling the close button click.
 */
class QuashAudioAdapter(private val onAudioCloseListener: OnAudioCloseListener) :
    ListAdapter<Uri, QuashAudioAdapter.QuashAudioViewHolder>(AudioComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuashAudioViewHolder {
        val binding =
            QuashItemAudioBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return QuashAudioViewHolder(binding, onAudioCloseListener)
    }

    override fun onBindViewHolder(holder: QuashAudioViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * ViewHolder class for audio items.
     *
     * @property binding The view binding for the audio item.
     * @property onCloseClickListener Listener for handling the close button click.
     */
    inner class QuashAudioViewHolder(
        private val binding: QuashItemAudioBinding,
        private val onCloseClickListener: OnAudioCloseListener
    ) : RecyclerView.ViewHolder(binding.root) {

        /**
         * Binds the audio URI to the view.
         *
         * @param item The URI of the audio file.
         */
        @SuppressLint("SetTextI18n")
        fun bind(item: Uri) {
            val filePath = item.path ?: return
            val file = File(filePath)
            if (!file.exists()) return

            val durationInSeconds = getAudioDurationInSeconds(item)
            binding.tvAudio.text = "${durationInSeconds}s"

            binding.ivCLose.setOnClickListener {
                file.delete()
                onCloseClickListener(item)
            }
        }

        /**
         * Retrieves the duration of the audio file in seconds.
         *
         * @param uri The URI of the audio file.
         * @return The duration of the audio file in seconds.
         */
        private fun getAudioDurationInSeconds(uri: Uri): Long {
            val mmr = MediaMetadataRetriever()
            return try {
                mmr.setDataSource(binding.root.context, uri)
                val durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val duration = durationStr?.toLongOrNull() ?: 0L
                TimeUnit.MILLISECONDS.toSeconds(duration)
            } catch (e: Exception) {
                DEFAULT_DURATION_IN_SECONDS
            }
        }
    }

    companion object {
        private const val DEFAULT_DURATION_IN_SECONDS = 12L
    }
}
