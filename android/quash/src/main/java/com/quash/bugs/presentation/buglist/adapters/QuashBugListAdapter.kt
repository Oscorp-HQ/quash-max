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
import androidx.core.content.ContextCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quash.bugs.R
import com.quash.bugs.core.data.dto.Report
import com.quash.bugs.databinding.QuashItemBugListBinding
import com.quash.bugs.presentation.helper.EditBugListener

/**
 * Adapter for displaying the list of bug reports.
 */
class BugsListAdapter(private val listener: EditBugListener) :
    PagingDataAdapter<Report, RecyclerView.ViewHolder>(BugsListComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        BugsListViewHolder(
            QuashItemBugListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            ), listener
        )

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as BugsListViewHolder
        getItem(position)?.let { holder.setData(it) }
    }
}

/**
 * ViewHolder for bug reports.
 */
class BugsListViewHolder(
    private val binding: QuashItemBugListBinding,
    private val listener: EditBugListener
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Binds the data to the view.
     */
    fun setData(item: Report) {
        with(binding) {
            when (item.type) {
                BUG -> {
                    ivType.background =
                        ContextCompat.getDrawable(binding.root.context, R.drawable.quash_ic_bug)
                }
                REPORT -> {
                    ivType.background =
                        ContextCompat.getDrawable(binding.root.context, R.drawable.quash_ic_crash)
                }
                else -> {
                    ivType.background =
                        ContextCompat.getDrawable(binding.root.context, R.drawable.quash_ic_ui)
                }
            }
            tvBugId.text = item.id
            tvBugTitle.text = item.title
        }

        binding.ivMenu.setOnClickListener {
            listener(item, true)
        }

        binding.root.setOnClickListener {
            listener(item, false)
        }
    }

    companion object {
        private const val BUG = "BUG"
        private const val REPORT = "CRASH"
    }
}

