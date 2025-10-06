/*
 * Moshi Companion - Companion to Moshi with more practical features.
 * Copyright (C) 2019 HighCapable
 * https://github.com/HighCapable/moshi-companion
 *
 * Apache License Version 2.0
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * This file is created by fankes on 2025/10/2.
 */
package com.highcapable.moshi.companion.api

import com.highcapable.moshi.companion.api.MoshiCompanion.AdapterRegistry
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Moshi.Builder
import java.lang.reflect.Type

/**
 * Moshi Companion core API.
 */
object MoshiCompanion {

    /**
     * Add an [AdapterRegistry] on this [Moshi.Builder] instance to register all [JsonAdapter]s
     * declared in the [AdapterRegistry.adapters] map when [Moshi.Builder.build] is called.
     * @see Moshi.Builder.addRegistry
     * @param builder the instance.
     * @param adapterRegistry the [AdapterRegistry] instance to set.
     * @return [Moshi.Builder]
     */
    @JvmStatic
    fun addRegistry(builder: Builder, adapterRegistry: AdapterRegistry) = apply {
        builder.add(AdapterRegistryFactory(adapterRegistry))
    }

    /**
     * A registry of [JsonAdapter]s to be registered with a [Moshi] instance.
     *
     * To use, create a subclass and implement [adapters]. Then add an instance of your subclass on
     * [Moshi.Builder.addRegistry].
     *
     * Example:
     * ```
     * class MyAdapterRegistry : MoshiCompanion.AdapterRegistry {
     *
     *     override val adapters = mapOf(
     *         MyType::class.java to MyTypeJsonAdapter::class.java,
     *         MyType.SubType::class.java to MyType_SubTypeJsonAdapter::class.java
     *     )
     * }
     *
     * val myRegistry = MyAdapterRegistry()
     *
     * val builder = Moshi.Builder().addRegistry(myRegistry)
     * val moshi = builder.build()
     * ```
     */
    interface AdapterRegistry {

        /**
         * A map of [Type] to [JsonAdapter] to be registered.
         */
        val adapters: Map<Type, Class<out JsonAdapter<*>>>
    }
}

/**
 * Add an [AdapterRegistry] on this [Moshi.Builder] instance to register all [JsonAdapter]s
 * declared in the [AdapterRegistry.adapters] map when [Moshi.Builder.build] is called.
 * @see MoshiCompanion.addRegistry
 * @receiver [Moshi.Builder] instance.
 * @param adapterRegistry the [AdapterRegistry] instance to set.
 * @return [Moshi.Builder]
 */
@JvmSynthetic
fun Builder.addRegistry(adapterRegistry: AdapterRegistry) = apply {
    add(AdapterRegistryFactory(adapterRegistry))
}

/**
 * Returns a JSON adapter for reified type parameter [T], creating it if necessary.
 *
 * Usage:
 *
 * ```kotlin
 * val moshi = Moshi.Builder().build()
 * val adapter = moshi.typeAdapter<List<String>>()
 * ```
 * @receiver [Moshi] instance.
 * @see typeRef
 * @return [JsonAdapter]<[T]>
 */
inline fun <reified T : Any> Moshi.typeAdapter(): JsonAdapter<T> = adapter(typeRef<T>().type)