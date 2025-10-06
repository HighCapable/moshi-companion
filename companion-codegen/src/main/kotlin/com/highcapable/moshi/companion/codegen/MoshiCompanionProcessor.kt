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
@file:Suppress("unused")

package com.highcapable.moshi.companion.codegen

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.KSAnnotated
import com.highcapable.moshi.companion.codegen.subprocessor.AdapterRegistryGenerator

@AutoService(SymbolProcessorProvider::class)
class MoshiCompanionProcessor : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment) = object : SymbolProcessor {

        private val subProcessor = listOf(
            AdapterRegistryGenerator(environment)
        )

        override fun process(resolver: Resolver) = emptyList<KSAnnotated>().let { startProcess(resolver); it }

        private fun startProcess(resolver: Resolver) {
            subProcessor.forEach {
                it.startProcess(resolver)
            }
        }
    }
}