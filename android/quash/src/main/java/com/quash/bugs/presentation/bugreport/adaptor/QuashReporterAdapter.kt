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

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.quash.bugs.core.data.dto.ReporterInt
import com.quash.bugs.core.util.asInitials
import com.quash.bugs.databinding.QuashItemReporterBinding
import com.quash.bugs.presentation.helper.OnReporterNameSelected

/**
 * Adapter for displaying a list of reporters in a RecyclerView.
 *
 * @property onReporterNameSelected Listener for handling reporter name selection.
 * @property name The name of the selected reporter.
 */
class QuashReporterAdapter(
    private val onReporterNameSelected: OnReporterNameSelected,
    private val name: String
) : ListAdapter<ReporterInt, QuashReporterViewHolder>(ReporterComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuashReporterViewHolder =
        QuashReporterViewHolder(
            QuashItemReporterBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            ), onReporterNameSelected, reporterName = name
        )

    override fun onBindViewHolder(holder: QuashReporterViewHolder, position: Int) {
        holder.setData(getItem(position))
    }
}

/**
 * ViewHolder for displaying reporter information.
 *
 * @property binding The view binding for the reporter item.
 * @property onReporterNameSelected Listener for handling reporter name selection.
 * @property reporterName The name of the selected reporter.
 */
class QuashReporterViewHolder(
    private val binding: QuashItemReporterBinding,
    private val onReporterNameSelected: OnReporterNameSelected,
    private val reporterName: String
) : RecyclerView.ViewHolder(binding.root) {

    /**
     * Binds the reporter data to the ViewHolder.
     *
     * @param item The reporter data to bind.
     */
    fun setData(item: ReporterInt) {
        binding.ivIntials.text = item.name.asInitials()
        binding.tvName.text = item.name
        binding.ivRight.visibility = if (reporterName == item.name) View.VISIBLE else View.GONE

        binding.root.setOnClickListener {
            onReporterNameSelected.invoke(item.name, item.id)
        }
    }
}

/**
 * DiffUtil callback for calculating the differences between two lists of ReporterInt objects.
 */
object ReporterComparator : DiffUtil.ItemCallback<ReporterInt>() {
    override fun areItemsTheSame(oldItem: ReporterInt, newItem: ReporterInt): Boolean {
        return oldItem.name == newItem.name
    }

    override fun areContentsTheSame(oldItem: ReporterInt, newItem: ReporterInt): Boolean {
        return oldItem == newItem
    }
}
