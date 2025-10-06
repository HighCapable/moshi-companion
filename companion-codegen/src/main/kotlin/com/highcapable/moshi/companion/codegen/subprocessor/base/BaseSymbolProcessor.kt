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
package com.highcapable.moshi.companion.codegen.subprocessor.base

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment

abstract class BaseSymbolProcessor(protected open val environment: SymbolProcessorEnvironment) {

    protected val logger by lazy { environment.logger }
    protected val codeGenerator by lazy { environment.codeGenerator }

    abstract fun startProcess(resolver: Resolver)
}