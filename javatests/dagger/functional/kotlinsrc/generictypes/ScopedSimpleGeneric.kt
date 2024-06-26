/*
 * Copyright (C) 2023 The Dagger Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dagger.functional.kotlinsrc.generictypes

import javax.inject.Inject
import javax.inject.Singleton

/**
 * An [Inject]ed generic class with no dependencies. Its factory class will have a generic
 * `create()` method returning an object whose type parameters cannot be inferred from its
 * arguments. Since it's scoped, the initialization of its field in a generated component must use a
 * raw [javax.inject.Provider] in order to allow casting from
 * `Provider<ScopedSimpleGeneric<Object>>` to `Provider<ScopedSimpleGeneric<Foo>>`.
 */
@Singleton class ScopedSimpleGeneric<T> @Inject constructor()
