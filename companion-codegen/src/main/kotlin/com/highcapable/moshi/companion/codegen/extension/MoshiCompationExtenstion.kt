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
package com.highcapable.moshi.companion.codegen.extension

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.symbol.KSAnnotated
import java.security.MessageDigest

@OptIn(KspExperimental::class)
inline fun <reified T : Annotation> KSAnnotated.findAnnotationWithType() = getAnnotationsByType(T::class).firstOrNull()

object HashString {

    private val md = MessageDigest.getInstance("SHA-256")

    fun generate(input: String, length: Int = 16): String {
        val digest = md.digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }.take(length)
    }
}