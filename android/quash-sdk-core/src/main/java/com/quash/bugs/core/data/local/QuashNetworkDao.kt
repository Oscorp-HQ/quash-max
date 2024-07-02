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
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "quashNetworkFilePath")
data class QuashNetworkFilePath(

    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val quashLogFilePath: String
)


@Dao
interface QuashNetworkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertQuashFilePath(quashFlePath: QuashNetworkFilePath)

    @Query("SELECT * FROM quashNetworkFilePath LIMIT 1")
    fun getQuashFilePath(): QuashNetworkFilePath?

    @Query("DELETE FROM quashNetworkFilePath")
    fun deleteAll()
}