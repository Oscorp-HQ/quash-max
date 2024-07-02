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

package com.quash.bugs.core.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.quash.bugs.core.data.dto.Report
import com.quash.bugs.core.data.remote.QuashApiService
import retrofit2.HttpException
import java.io.IOException

/**
 * A PagingSource implementation for loading bug reports from the Quash API.
 *
 * @param apiService The QuashApiService instance used to fetch data.
 * @param appId The application ID for which to fetch bug reports.
 */
class QuashBugListDataSource(private val apiService: QuashApiService, private val appId: String) :
    PagingSource<Int, Report>() {

    /**
     * Loads a page of data from the Quash API.
     *
     * @param params The parameters for loading data, including the key for the next page.
     * @return The result of the load operation, which is either a page of data or an error.
     */
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Report> {
        val pageNumber = params.key ?: 0
        return try {
            val response = apiService.getBugs(appId, pageNumber, 10)
            val pagedResponse = response.body()
            val data = pagedResponse?.data?.reports
            data?.let {
                LoadResult.Page(
                    data = it,
                    prevKey = if (pageNumber == 0) null else pageNumber - 1,
                    nextKey = if (it.isEmpty()) null else pageNumber + 1
                )
            } ?: LoadResult.Error(IOException("Error " + response.code()))
        } catch (exception: IOException) {
            // Handle network errors or other I/O exceptions
            val error = IOException("Please Check Internet Connection")
            LoadResult.Error(error)
        } catch (exception: HttpException) {
            // Handle HTTP errors
            LoadResult.Error(exception)
        }
    }

    /**
     * Returns the key for the page to be refreshed based on the current PagingState.
     *
     * @param state The current state of the paging system.
     * @return The key for the page to be refreshed.
     */
    override fun getRefreshKey(state: PagingState<Int, Report>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}
