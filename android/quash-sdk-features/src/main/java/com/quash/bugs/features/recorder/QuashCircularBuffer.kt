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

package com.quash.bugs.features.recorder

/**
 * A generic circular buffer implementation.
 *
 * This buffer maintains a fixed capacity and overwrites the oldest entries
 * when new entries are added beyond the capacity. It is designed to be
 * thread-safe for concurrent access.
 *
 * @param capacity The maximum number of elements the buffer can hold.
 * @param T The type of elements stored in the buffer.
 */
class QuashCircularBuffer<T>(private val capacity: Int) {
    // Underlying array to store buffer elements
    private val buffer = arrayOfNulls<Any>(capacity)

    // Current number of elements in the buffer
    var size = 0

    // Index of the oldest element in the buffer
    private var start = 0

    /**
     * Adds an item to the buffer.
     *
     * If the buffer is full, the oldest item will be overwritten.
     *
     * @param item The item to add to the buffer.
     */
    @Synchronized
    fun add(item: T) {
        if (size == capacity) {
            // If the buffer is full, increment the start index to overwrite the oldest element
            start = (start + 1) % capacity
        } else {
            // Otherwise, increase the size of the buffer
            size++
        }
        // Add the new item at the end of the buffer, wrapping around if necessary
        buffer[(start + size - 1) % capacity] = item
    }

    /**
     * Retrieves all items from the buffer.
     *
     * The items are returned in the order they were added.
     *
     * @return A list of all items in the buffer.
     */
    @Synchronized
    fun getAll(): List<T> {
        val list = mutableListOf<T>()
        for (i in 0 until size) {
            // Add elements to the list in the correct order
            list.add(buffer[(start + i) % capacity] as T)
        }
        return list
    }

    /**
     * Clears all items from the buffer.
     *
     * Resets the buffer to its initial state.
     */
    @Synchronized
    fun clear() {
        // Set all elements in the buffer to null
        for (i in buffer.indices) {
            buffer[i] = null
        }
        // Reset size and start index
        size = 0
        start = 0
    }
}
