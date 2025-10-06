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

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.toClassOrNull
import com.highcapable.kavaref.resolver.ConstructorResolver
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * [MoshiCompanion] registered [JsonAdapter.Factory] implementation that loads
 * generated [JsonAdapter]s from the given [MoshiCompanion.AdapterRegistry].
 */
internal class AdapterRegistryFactory(private val adapterRegistry: MoshiCompanion.AdapterRegistry) : JsonAdapter.Factory {

    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val rawType = type.toClassOrNull() ?: return null

        // Check if there is annotation for `@JsonClass` and `generateAdapter = true`.
        val jsonClass = rawType.getAnnotation(classOf<JsonClass>())
        if (jsonClass == null || !jsonClass.generateAdapter) return null

        var possiblyFoundAdapter: Class<out JsonAdapter<*>>? = null

        return try {
            val adapterClass = adapterRegistry.adapters.firstNotNullOfOrNull { (regType, regAdapter) ->
                if (Types.equals(rawType, regType))
                    regAdapter
                else null
            } ?: run {
                // Fallback to reflective lookup.
                val rawAdapterClassName = Types.generatedJsonAdapterName(rawType.name)
                rawAdapterClassName.toClassOrNull<JsonAdapter<*>>() ?: throw ClassNotFoundException()
            }
            possiblyFoundAdapter = adapterClass

            val constructor: ConstructorResolver<out JsonAdapter<*>>
            val args: Array<Any>

            if (type is ParameterizedType) {
                val typeArgs = type.actualTypeArguments

                // Common case first.
                val twoParams = adapterClass.resolve()
                    .optional(silent = true)
                    .constructor {
                        parameters(Moshi::class, Array<Type>::class)
                    }.firstOrNull()
                val oneParam = adapterClass.resolve()
                    .optional(silent = true)
                    .constructor { parameters(Moshi::class) }
                    .firstOrNull()

                constructor = twoParams ?: oneParam ?: throw NoSuchMethodException()
                args = if (twoParams != null)
                    arrayOf(moshi, typeArgs)
                else arrayOf(moshi)
            } else {
                val oneParam = adapterClass.resolve()
                    .optional(silent = true)
                    .constructor { parameters(Moshi::class) }
                    .firstOrNull()
                val noParams = adapterClass.resolve()
                    .optional(silent = true)
                    .constructor { emptyParameters() }
                    .firstOrNull()

                constructor = oneParam ?: noParams ?: throw NoSuchMethodException()
                args = if (oneParam != null)
                    arrayOf(moshi)
                else emptyArray()
            }

            constructor.createQuietly(*args)?.nullSafe()
        } catch (_: ClassNotFoundException) {
            // If it is not found at all, return null and let other factory handle it.
            null
        } catch (e: NoSuchMethodException) {
            if (possiblyFoundAdapter != null && type !is ParameterizedType && possiblyFoundAdapter.typeParameters.isNotEmpty())
                throw RuntimeException(
                    "Failed to find the generated JsonAdapter constructor for '$type'. " +
                        "Suspiciously, the type was not parameterized but the target class " +
                        "'${possiblyFoundAdapter.canonicalName}' is generic. " +
                        "Consider using Types#newParameterizedType() to define these missing type variables.",
                    e
                )
            else throw RuntimeException(
                "Failed to find the generated JsonAdapter constructor for $type. " +
                    "Target adapter class is '${possiblyFoundAdapter?.canonicalName}'.",
                e
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to create JsonAdapter for $type.", e)
        }
    }
}