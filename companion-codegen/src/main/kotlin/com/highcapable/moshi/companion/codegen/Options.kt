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
 * This file is created by fankes on 2025/10/3.
 */
package com.highcapable.moshi.companion.codegen

object Options {

    /**
     * This boolean processing option is used to read the parameters of the Moshi
     * ontology project "moshi-kotlin-codegen", which should be configured by the user as "false".
     */
    const val OPTION_GENERATE_PROGUARD_RULES_MOSHI = "moshi.generateProguardRules"

    /**
     * This string processing option can change the package name of the generated AdapterRegistry implementation class.
     * Default is `com.highcapable.moshi.companion.generated` + no repetition package name hash.
     */
    const val OPTION_GENERATE_ADAPTER_REGISTRY_PACKAGE_NAME = "moshi-companion.generateAdapterRegistryPackageName"

    /**
     * This string processing option can change the class name of the generated AdapterRegistry implementation class.
     * Default is `DefaultMoshiAdapterRegistry`.
     */
    const val OPTION_GENERATE_ADAPTER_REGISTRY_CLASS_NAME = "moshi-companion.generateAdapterRegistryClassName"

    /**
     * This boolean processing option can change the access modifier of the generated AdapterRegistry implementation class.
     * If true, the class will have internal access; if false, it will have public access.
     * This is disabled by default.
     */
    const val OPTION_GENERATE_ADAPTER_REGISTRY_RESTRICTED_ACCESS = "moshi-companion.generateAdapterRegistryRestrictedAccess"

    /**
     * This boolean processing option can disable proguard rule generation.
     * Normally, this is not recommended unless end-users build their own JsonAdapter look-up tool.
     * This is enabled by default.
     */
    const val OPTION_GENERATE_PROGUARD_RULES = "moshi-companion.generateProguardRules"

    /**
     * This boolean processing option can enable keeping enum classes values method in proguard rules.
     * Normally, this is not recommended unless you are sure that you don't need enum values method.
     * This is enabled by default.
     */
    const val OPTION_PROGUARD_RULES_KEEP_ENUM_CLASSES = "moshi-companion.proguardRulesKeepEnumClasses"
}