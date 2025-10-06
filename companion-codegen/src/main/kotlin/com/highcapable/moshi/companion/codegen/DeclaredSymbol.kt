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
@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.highcapable.moshi.companion.codegen

object DeclaredSymbol {

    const val MOSHI_PACKAGE_NAME = "com.squareup.moshi"

    const val MOSHI_CLASS_NAME = "Moshi"
    const val MOSHI_CLASS = "$MOSHI_PACKAGE_NAME.$MOSHI_CLASS_NAME"

    const val JSON_ANNOTATION_CLASS_NAME = "JsonClass"
    const val JSON_ANNOTATION_CLASS = "$MOSHI_PACKAGE_NAME.$JSON_ANNOTATION_CLASS_NAME"

    const val JSON_ADAPTER_CLASS_NAME = "JsonAdapter"
    const val JSON_ADAPTER_CLASS = "$MOSHI_PACKAGE_NAME.$JSON_ADAPTER_CLASS_NAME"

    const val MOSHI_COMPANION_API_PACKAGE_NAME = "com.highcapable.moshi.companion.api"

    const val MOSHI_COMPANION_CLASS_NAME = "MoshiCompanion"
    const val MOSHI_COMPANION_CLASS = "$MOSHI_COMPANION_API_PACKAGE_NAME.$MOSHI_COMPANION_CLASS_NAME"

    const val TYPE_REF_CLASS_NAME = "TypeRef"
    const val TYPE_REF_CLASS = "$MOSHI_COMPANION_API_PACKAGE_NAME.$TYPE_REF_CLASS_NAME"

    const val ADAPTER_REGISTRY_CLASS_NAME = "AdapterRegistry"
}