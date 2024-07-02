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

package com.quash.bugs.presentation.helper

import com.quash.bugs.R
import com.quash.bugs.core.data.dto.QuashPriority

/**
 * Object containing functions to generate lists of QuashPriority objects.
 */
object QuashPriorityGenerator {

    /**
     * Function to generate a list of QuashPriority objects representing priority levels.
     * @return a List of QuashPriority objects representing priority levels.
     */
    fun priorityList(): List<QuashPriority> {
        val list = arrayListOf<QuashPriority>()
        list.add(QuashPriority(R.drawable.quash_ic_high, "High", "HIGH"))
        list.add(QuashPriority(R.drawable.quash_ic_medium, "Medium", "MEDIUM"))
        list.add(QuashPriority(R.drawable.quash_ic_low, "Low", "LOW"))
        list.add(QuashPriority(R.drawable.quash_ic_not_define, "Not Defined", "NOT_DEFINED"))
        return list
    }

    /**
     * Function to generate a list of QuashPriority objects representing bug types.
     * @return a List of QuashPriority objects representing bug types.
     */
    fun typeList(): List<QuashPriority> {
        val list = arrayListOf<QuashPriority>()
        list.add(QuashPriority(R.drawable.quash_ic_bug, "Bug", "BUG"))
        list.add(QuashPriority(R.drawable.quash_ic_ui, "UI Improvement", "UI"))
        list.add(QuashPriority(R.drawable.quash_ic_crash, "Crash", "CRASH"))
        return list
    }
}