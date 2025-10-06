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
 * This file is created by fankes on 2025/10/6.
 */
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.highcapable.moshi.companion.api

import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.toClass
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * Type reference class for getting generic parameter [T] type.
 *
 * The purpose of this class is to retain erased generics at runtime.
 * @see typeRef
 */
abstract class TypeRef<T : Any> {

    /**
     * Get the generic parameter [T] type.
     * @return [Type]
     */
    val type by lazy {
        when (val superclass = javaClass.genericSuperclass) {
            is ParameterizedType ->
                if (superclass.rawType == classOf<TypeRef<*>>()) 
                    superclass.actualTypeArguments.firstOrNull() ?: error("Type argument cannot be null.")
                else error("Must only create direct subclasses of TypeRef.")
            classOf<TypeRef<*>>() -> error("TypeRef must be created with a type argument: object : TypeRef<...>() {}.")
            else -> error("Must only create direct subclasses of TypeRef.")
        }
    }

    /**
     * Get the raw class type of the generic parameter [T].
     * @return [Class]
     */
    val rawType by lazy {
        when (val currentType = type) {
            is Class<*> -> currentType
            is ParameterizedType -> currentType.toClass()
            else -> classOf<Any>()
        }
    }

    /**
     * Checks if the specified [other] type can be assigned to this type.
     * @param other the type to check.
     * @return `true` if the specified type can be assigned to this type, `false` otherwise.
     */
    fun isAssignableFrom(other: Type) = when {
        type is Class<*> && other is Class<*> -> rawType.isAssignableFrom(other)
        else -> type == other
    }

    override fun toString() = type.toString()
    override fun equals(other: Any?) = other is TypeRef<*> && type == other.type
    override fun hashCode() = type.hashCode()
}

/**
 * Create a [TypeRef] instance with the reified type parameter [T].
 *
 * Usage:
 *
 * ```kotlin
 * val typeRef = typeRef<List<String>>()
 * // This will be of type `List<String>`.
 * val type = typeRef.type
 * // This will be of type `List`.
 * val rawType = typeRef.rawType
 * ```
 * @see TypeRef
 * @return [TypeRef]<[T]>
 */
inline fun <reified T : Any> typeRef() = object : TypeRef<T>() {}