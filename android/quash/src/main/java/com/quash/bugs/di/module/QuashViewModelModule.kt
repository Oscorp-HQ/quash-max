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

package com.quash.bugs.di.module


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.quash.bugs.presentation.buglist.viewmodel.QuashBugListViewModel
import com.quash.bugs.presentation.bugreport.viewmodel.QuashBugReportViewModel
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * A Dagger module for providing ViewModel instances to facilitate the MVVM architecture in the application.
 * This module binds ViewModel instances to a ViewModelProvider.Factory, allowing for dependency injection in ViewModels,
 * which simplifies their construction and management of their dependencies.
 */
@Module
abstract class QuashViewModelModule {

    /**
     * Binds a custom ViewModelFactory to the ViewModelProvider.Factory.
     * This factory is responsible for creating ViewModel instances, ensuring they can be injected with dependencies.
     */
    @Binds
    @Singleton
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    /**
     * Binds the ReportingViewModel to the ViewModel multibindings map.
     * This allows the ViewModel to be constructed with its dependencies injected.
     */
    @Binds
    @IntoMap
    @Singleton
    @ViewModelKey(QuashBugReportViewModel::class)
    abstract fun bindReportingViewModel(viewModel: QuashBugReportViewModel): ViewModel

    /**
     * Binds the QuashBugListViewModel to the ViewModel multibindings map.
     * Similar to ReportingViewModel, it facilitates dependency injection into the ViewModel.
     */
    @Binds
    @IntoMap
    @Singleton
    @ViewModelKey(QuashBugListViewModel::class)
    abstract fun bindBugListViewModel(viewModel: QuashBugListViewModel): ViewModel
}

/**
 * Factory for creating ViewModel instances. Utilizes a map of Providers to instantiate ViewModels
 * with necessary dependencies injected.
 */
@Singleton
class ViewModelFactory @Inject constructor(
    private val viewModels: MutableMap<Class<out ViewModel>,
            @JvmSuppressWildcards Provider<ViewModel>>
) : ViewModelProvider.Factory {

    /**
     * Creates a ViewModel instance, identified by its class type [T].
     * This method fetches the ViewModel from the map, using the provided class as a key.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        viewModels[modelClass]?.get() as T
}

/**
 * Annotation used for mapping ViewModels to their classes in Dagger multibindings.
 * This allows ViewModels to be dynamically injected by their class types.
 */
@Target(
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER
)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
internal annotation class ViewModelKey(val value: KClass<out ViewModel>)
