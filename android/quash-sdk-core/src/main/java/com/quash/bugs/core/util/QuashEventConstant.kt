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

package com.quash.bugs.core.util

/**
 * Object containing constants used for logging events.
 */
object QuashEventConstant {

    /**
     * Object containing constants related to application information.
     */
    object App {
        /**
         * Constant representing the name of the application.
         */
        const val NAME = "app_name"

        /**
         * Constant representing the ID of the application.
         */
        const val ID = "app_id"

        /**
         * Constant representing the type of the application.
         */
        const val TYPE = "app_type"

        /**
         * Constant representing the package ID of the application.
         */
        const val PACKAGE_ID = "package_id"
    }

    /**
     * Object containing constants related to organization information.
     */
    object Org {
        /**
         * Constant representing the unique key of the organization.
         */
        const val UNIQUE_KEY = "org_key"
    }

    /**
     * Object containing constants related to event information.
     */
    object Event {
        /**
         * Constant representing the source of the event.
         */
        const val SOURCE = "source"

        /**
         * Constant representing the name of the event.
         */
        const val NAME = "event_name"
    }
}

/**
 * Sealed class representing the possible sources of an event.
 */
sealed class EventSource {
    /**
     * Event source indicating a user interaction.
     */
    data object UserInteraction : EventSource()

    /**
     * Event source indicating a system event.
     */
    data object SystemEvent : EventSource()

    /**
     * Event source indicating a network request.
     */
    data object NetworkRequest : EventSource()
}