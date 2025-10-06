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
package com.highcapable.moshi.companion.codegen.subprocessor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.highcapable.moshi.companion.codegen.DeclaredSymbol
import com.highcapable.moshi.companion.codegen.Options
import com.highcapable.moshi.companion.codegen.extension.HashString
import com.highcapable.moshi.companion.codegen.extension.findAnnotationWithType
import com.highcapable.moshi.companion.codegen.subprocessor.base.BaseSymbolProcessor
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.moshi.JsonClass
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

class AdapterRegistryGenerator(override val environment: SymbolProcessorEnvironment) : BaseSymbolProcessor(environment) {

    private companion object {

        private const val ADAPTER_REGISTRY_PACKAGE_NAME_SUFFIX = "generated"
        private const val DEFAULT_ADAPTER_REGISTRY_PACKAGE_NAME_PREFIX = "com.highcapable.moshi.companion"
        private const val DEFAULT_ADAPTER_REGISTRY_CLASS_NAME = "DefaultMoshiAdapterRegistry"

        val JsonAdapterClass = ClassName(DeclaredSymbol.MOSHI_PACKAGE_NAME, DeclaredSymbol.JSON_ADAPTER_CLASS_NAME)
        val AdapterRegistryClass = ClassName(DeclaredSymbol.MOSHI_COMPANION_API_PACKAGE_NAME, DeclaredSymbol.MOSHI_COMPANION_CLASS_NAME)
            .nestedClass(DeclaredSymbol.ADAPTER_REGISTRY_CLASS_NAME)
        val TypeClass = ClassName("java.lang.reflect", "Type")
    }

    private val generateAdapterRegistryPackageName = environment.options[Options.OPTION_GENERATE_ADAPTER_REGISTRY_PACKAGE_NAME]
    private val generateAdapterRegistryClassName = environment.options[Options.OPTION_GENERATE_ADAPTER_REGISTRY_CLASS_NAME]
    private val generateAdapterRegistryRestrictedAccess = environment.options[Options.OPTION_GENERATE_ADAPTER_REGISTRY_RESTRICTED_ACCESS]
        ?.toBooleanStrictOrNull() ?: false
    private val generateProguardRulesMoshi = environment.options[Options.OPTION_GENERATE_PROGUARD_RULES_MOSHI]?.toBooleanStrictOrNull()
    private val generateProguardRules = environment.options[Options.OPTION_GENERATE_PROGUARD_RULES]?.toBooleanStrictOrNull() ?: true
    private val keepEnumClasses = environment.options[Options.OPTION_PROGUARD_RULES_KEEP_ENUM_CLASSES]?.toBooleanStrictOrNull() ?: true

    private val adapterRegistrations = mutableListOf<AdapterRegistration>()
    private var originatingFile: KSFile? = null

    override fun startProcess(resolver: Resolver) {
        processRegistrations(resolver)
        generateCodeFile()
        generateProguardFile()
    }

    private fun processRegistrations(resolver: Resolver) {
        resolver.getSymbolsWithAnnotation(JsonClass::class.qualifiedName!!).forEach { type ->
            // For the smart cast.
            if (type !is KSDeclaration) return@forEach

            // Use the first obtained source file as the dependency of the generated file.
            if (originatingFile == null)
                originatingFile = type.containingFile ?: return@forEach

            val jsonClassAnnotation = type.findAnnotationWithType<JsonClass>() ?: return@forEach

            if (!jsonClassAnnotation.generateAdapter) return@forEach

            runCatching {
                // Collect adapter registration information.
                if (type is KSClassDeclaration) {
                    val targetClassName = type.toClassName()
                    val adapterClassName = ClassName(
                        targetClassName.packageName,
                        "${targetClassName.simpleNames.joinToString("_")}JsonAdapter"
                    )
                    val hasTypeParameters = type.typeParameters.isNotEmpty()

                    adapterRegistrations += AdapterRegistration(
                        targetClass = targetClassName,
                        adapterClass = adapterClassName,
                        hasTypeParameters = hasTypeParameters
                    )
                }
            }.onFailure {
                logger.error("Error preparing ${type.simpleName.asString()}\n${it.stackTraceToString()}")
            }
        }
    }

    private fun generateCodeFile() {
        if (adapterRegistrations.isNotEmpty()) {
            val fileSpec = generateAdapterRegistry()

            try {
                fileSpec.writeTo(codeGenerator, aggregating = true)
            } catch (_: FileAlreadyExistsException) {
                // Ignored, FileAlreadyExistsException can happen if multiple compilations.
            }
        }
    }

    private fun generateProguardFile() {
        if (generateProguardRules) require(generateProguardRulesMoshi == false) {
            "Proguard rules generation for Moshi is enabled. " +
                "Please disable it by setting the \"${Options.OPTION_GENERATE_PROGUARD_RULES_MOSHI}\" option to \"false\" " +
                "in your build configuration to avoid duplicate rules."
        }

        if (generateProguardRules) {
            val config = ProguardConfig(adapterRegistrations, keepEnumClasses)

            try {
                config.writeTo(codeGenerator)
            } catch (_: FileAlreadyExistsException) {
                // Ignored, FileAlreadyExistsException can happen if multiple compilations.
            }
        }
    }

    private fun generateAdapterRegistry(): FileSpec {
        // Get the top-level package name of the project.
        val packageName = run {
            adapterRegistrations
                .filterByPackageNameFirstOrNull()
                ?.targetClass?.packageName
        } ?: "default" // fallback package name

        val registryPackageName = (generateAdapterRegistryPackageName ?: run {
            val packageHash = HashString.generate(packageName)
            "$DEFAULT_ADAPTER_REGISTRY_PACKAGE_NAME_PREFIX.r$packageHash"
        }) + ".$ADAPTER_REGISTRY_PACKAGE_NAME_SUFFIX"

        val adaptersMapBuilder = CodeBlock.builder()
        adaptersMapBuilder.add("\nmapOf(")

        adapterRegistrations.forEachIndexed { index, registration ->
            if (index > 0) adaptersMapBuilder.add(",")
            adaptersMapBuilder.add(
                "\n  %T::class.java to %T::class.java",
                registration.targetClass,
                registration.adapterClass
            )
        }

        if (adapterRegistrations.isNotEmpty())
            adaptersMapBuilder.add("\n")

        adaptersMapBuilder.add(")")

        val adaptersProperty = PropertySpec.builder(
            name = "adapters",
            Map::class.asClassName().parameterizedBy(
                TypeClass, Class::class.asClassName().parameterizedBy(
                    WildcardTypeName.producerOf(JsonAdapterClass.parameterizedBy(STAR))
                )
            )
        ).apply {
            addModifiers(KModifier.OVERRIDE)
            initializer(adaptersMapBuilder.build())
        }.build()

        val adapterRegistryClassName = generateAdapterRegistryClassName ?: DEFAULT_ADAPTER_REGISTRY_CLASS_NAME
        val registryClass = TypeSpec.classBuilder(adapterRegistryClassName).apply {
            if (generateAdapterRegistryRestrictedAccess)
                addModifiers(KModifier.INTERNAL)
            else addModifiers(KModifier.PUBLIC)
            addSuperinterface(AdapterRegistryClass)
            addProperty(adaptersProperty)
        }.build()

        val fileSpec = FileSpec.builder(registryPackageName, adapterRegistryClassName).apply {
            addFileComment(
                """
                  This file is auto generated by Moshi Companion CodeGen.
                  **DO NOT EDIT THIS FILE MANUALLY**
                """.trimIndent()
            )
            addType(registryClass)
        }.build()

        return fileSpec
    }

    private fun List<AdapterRegistration>.filterByPackageNameFirstOrNull(): AdapterRegistration? {
        val registrations = filter { it.targetClass.packageName.isNotEmpty() }
        if (registrations.isEmpty()) return null

        // Count the number of prefixes for each class name.
        val prefixCount = mutableMapOf<String, Int>()
        val splitMap = registrations.associateWith { it.targetClass.packageName.split(".") }

        splitMap.values.forEach { parts ->
            val prefix = parts.take(3).joinToString(".")
            prefixCount[prefix] = (prefixCount[prefix] ?: 0) + 1
        }

        // Find the prefix with the least number of occurrences.
        val oddPrefix = prefixCount.minByOrNull { it.value }?.key

        // Check whether there are different class names.
        val oddClassName = splitMap.filter { it.value.take(3).joinToString(".") == oddPrefix }.keys.firstOrNull()
        if (oddClassName != null && prefixCount[oddPrefix] == 1) return oddClassName

        // If there is no different style, return the shortest class name.
        return registrations.minByOrNull { it.targetClass.packageName.length }
    }

    private fun ProguardConfig.writeTo(codeGenerator: CodeGenerator) {
        val originatingFile = originatingFile ?: return

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true, originatingFile),
            packageName = "",
            fileName = outputFilePathWithoutExtension,
            extensionName = "pro"
        )
        OutputStreamWriter(file, StandardCharsets.UTF_8).use(::writeTo)
    }

    private data class AdapterRegistration(
        val targetClass: ClassName,
        val adapterClass: ClassName,
        val hasTypeParameters: Boolean
    )

    private data class ProguardConfig(
        private val registrations: List<AdapterRegistration>,
        private val keepEnumClasses: Boolean
    ) {

        private companion object {

            const val DEFAULT_OUTPUT_FILE_PATH = "META-INF/proguard/moshi-companion"
        }

        val outputFilePathWithoutExtension
            get() = "$DEFAULT_OUTPUT_FILE_PATH-r${HashString.generate(registrations.first().targetClass.canonicalName)}"

        fun writeTo(out: Appendable) = out.run {
            // Keep the `DefaultConstructorMarker` class, needed for synthetic constructors with default parameters.
            appendLine("-keepnames class kotlin.jvm.internal.DefaultConstructorMarker")
            appendLine()

            // Keep the `JsonClass` annotation on the target class.
            appendLine("-keep,allowobfuscation @${DeclaredSymbol.JSON_ANNOTATION_CLASS} class *")
            appendLine()

            // Keep the `TypeRef` class and its subclasses.
            appendLine("-keep,allowobfuscation class ${DeclaredSymbol.TYPE_REF_CLASS} {")
            appendLine("    <fields>;")
            appendLine("    <methods>;")
            appendLine("}")
            appendLine()

            appendLine("-keep,allowobfuscation class * extends ${DeclaredSymbol.TYPE_REF_CLASS}")
            appendLine()

            // Keep generic signatures.
            appendLine("-keepattributes Signature")
            appendLine()

            // Keep the enum values method.
            if (keepEnumClasses) {
                appendLine("-keepclassmembers enum * {")
                appendLine("    public static **[] values();")
                appendLine("    public static ** valueOf(java.lang.String);")
                appendLine("    public static <fields>;")
                appendLine("}")
                appendLine()
            }

            registrations.forEach { registration ->
                val targetName = registration.targetClass.reflectionName()
                val adapterCanonicalName = ClassName(
                    registration.targetClass.packageName,
                    registration.adapterClass.simpleName
                ).canonicalName
                
                // Keep the constructor for Moshi's reflective lookup.
                appendLine("-if class $targetName")
                appendLine("-keepclassmembers class $adapterCanonicalName {")

                if (registration.hasTypeParameters)
                    appendLine("    public <init>(${DeclaredSymbol.MOSHI_CLASS}, ${TypeClass.canonicalName}[]);")
                else appendLine("    public <init>(${DeclaredSymbol.MOSHI_CLASS});")

                appendLine("}")
                appendLine()

                // Keep the synthetic constructor if the target class has default parameter values.
                appendLine("-keepclassmembers class $targetName {")
                appendLine("    public synthetic <init>(...);")
                appendLine("}")
                appendLine()
            }
        }
    }
}