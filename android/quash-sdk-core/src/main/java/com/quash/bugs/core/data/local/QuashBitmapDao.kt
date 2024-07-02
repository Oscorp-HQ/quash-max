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

package com.quash.bugs.core.data.local


import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "quash_buffer_item")
data class QuashBufferItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val filePath: String,
    val timestamp: Long? = null,
)

@Dao
interface QuashBitmapDao {
    @Insert
    suspend fun insert(bitmapEntity: QuashBufferItem)

    @Query("SELECT * FROM quash_buffer_item")
    fun getAllBitmaps(): List<QuashBufferItem>

    @Query("SELECT * FROM quash_buffer_item WHERE timestamp BETWEEN :startTime AND :endTime")
    fun getScreenshotsInTimeRange(startTime: Long, endTime: Long): List<QuashBufferItem>

    @Query("DELETE FROM quash_buffer_item")
    suspend fun clearAll()
}