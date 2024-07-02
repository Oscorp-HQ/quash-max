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

/**
 * Enum representing different media options for Quash.
 *
 * @property id The unique identifier for each media option.
 */
enum class QuashMediaOption(val id: Int) {
    /**
     * Option for capturing a screenshot.
     */
    SCREENSHOT(0),

    /**
     * Option for recording the screen.
     */
    SCREEN_RECORD(1),

    /**
     * Option for opening the gallery.
     */
    OPEN_GALLERY(2);

    companion object {
        /**
         * Returns the QuashMediaOption enum entry based on the provided id.
         *
         * @param id The id of the desired QuashMediaOption.
         * @return The QuashMediaOption enum entry corresponding to the provided id.
         * @throws NoSuchElementException if no QuashMediaOption with the specified id exists.
         */
        fun fromId(id: Int) = entries.first { it.id == id }
    }
}