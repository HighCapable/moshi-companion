# Moshi Companion 使用文档

![Maven Central](https://img.shields.io/maven-central/v/com.highcapable.moshi.companion/companion-api?logo=apachemaven&logoColor=orange&style=flat-square)

在开始使用之前，建议你仔细阅读此文档，以便你能更好地了解它的作用方式与功能。

你可以在项目的根目录找到 samples 中的 Demo，并参考此文档食用效果更佳。

## 开始之前

此项目的主要功能是为 [Moshi](https://github.com/square/moshi) 提供伴侣功能，核心功能依赖于 Moshi 项目核心完成，你需要使用 Moshi 的 `moshi-kotlin` 和 `moshi-kotlin-codegen` 依赖来生成适配器类。

此项目的目的是将 `moshi-kotlin-codegen` 生成的适配器类生成 "实体类 → 适配器类" 的映射注册到 `AdapterRegistry` 中并创建自定义的 `JsonAdapter` 设置到 `Moshi.Builder`，从而避免 Moshi 通过 `Class.forName` 反射查找适配器类，达到完全混淆实体类名称和字段的目的，同时性能将由 O(n) 提升到 O(1)。

此项目主要专注于 Android 项目，在纯 Kotlin/Java 项目中依然可以使用。

## 快速开始

首先，在你的 Android/Kotlin/Java 项目中添加依赖，我们推荐直接使用 Gradle 的 Version Catalog 功能来管理依赖版本：

> `gradle/libs.versions.toml`

```toml
[versions]
agp = "8.13.0"
# Kotlin 相关依赖版本可从 https://kotlinlang.org/docs/releases.html 获取
kotlin = "2.2.20"
ksp = "2.2.20-2.0.3"
# Moshi 的版本建议使用 Moshi GitHub README 提供的版本
moshi = "1.15.2"
moshi-companion = "<version>"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-jvm = { id = "org.jetbrains.kotlin.jvm", version.ref = "kotlin" }
kotlin-ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }

[libraries]
moshi-kotlin = { module = "com.squareup.moshi:moshi-kotlin", version.ref = "moshi" }
moshi-kotlin-codegen = { module = "com.squareup.moshi:moshi-kotlin-codegen", version.ref = "moshi" }
moshi-companion-api = { module = "com.highcapable.moshi.companion:companion-api", version.ref = "moshi-companion" }
moshi-companion-codegen = { module = "com.highcapable.moshi.companion:companion-codegen", version.ref = "moshi-companion" }
```

将上述 `<version>` 的版本替换为顶部显示的最新版本号。

然后，在你需要使用 Moshi 的 Gradle 项目配置文件 `build.gradle` 或 `build.gradle.kts` 中添加如下配置：

```kotlin
plugins {
     // 如果是 Android 项目
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // KSP
    alias(libs.plugins.kotlin.ksp)
}

dependencies {
    // Moshi 相关依赖
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)

    // Moshi Companion 相关依赖
    implementation(libs.moshi.companion.api)
    // Moshi Companion Codegen 需要在 moshi-kotlin-codegen 之后添加，请注意顺序
    ksp(libs.moshi.companion.codegen)
}
```

然后我们需要确保关闭 Moshi 的混淆规则生成功能，因为混淆规则已经被 Moshi Companion 接管。

```kotlin
ksp {
    arg("moshi.generateProguardRules", "false")
}
```

如果你没有关闭 Moshi 的混淆规则生成功能，Moshi Companion 将会在编译时抛出异常提示你进行修正。

至此，你已经完成了 Moshi Companion 的依赖添加和配置，如果你是在使用 Moshi 的现有项目中添加 Moshi Companion，你只需要引入上述步骤中的 `moshi-companion-api` 和 `moshi-companion-codegen` 依赖，并添加关闭混淆规则生成的配置即可。

## 注册适配器

Moshi Companion 通过读取项目中所有使用 `@JsonClass(generateAdapter = true)` 注解的类来生成 `AdapterRegistry`，你需要在运行时通过 `Moshi.Builder` 手动注册这些适配器。

在执行过一次 Gradle 构建后，Moshi Companion 会在项目的 `build/generated/ksp` 目录下生成 `AdapterRegistry` 类，生成的默认格式为扫描到不重复的一个存在 `@JsonClass(generateAdapter = true)` 注解的包名，使用这个包名转换为 16 位的 Hash 作为当前模块的唯一标识，并生成如下格式的包名：

```
com.highcapable.moshi.companion.r + 16 位 Hash + generated
```

形如：

```
com.highcapable.moshi.companion.r1dd1c7f2a95790d7.generated
```

`AdapterRegistry` 的类名固定为 `DefaultMoshiAdapterRegistry`，实现了 `AdapterRegistry` 接口。

在使用 Moshi 时，你可以非常简单地使用扩展函数 `addRegistry` 将这个生成的类注册到 `Moshi.Builder` 中：

```kotlin
val moshi = Moshi.Builder()
    .addRegistry(DefaultMoshiAdapterRegistry())
    .build()
```

然后，你就可以愉快地继续使用 Moshi 进行 JSON 的序列化和反序列化了，完全不受 R8 的混淆和优化影响，类名可以完全做到安全混淆、压缩体积、减少反射和暴露风险。

## 高级用法

如果你需要自定义 `AdapterRegistry` 的类名和包名，可以通过在 `build.gradle` 或 `build.gradle.kts` 中添加如下 KSP 参数来实现：

```kotlin
ksp {
    // 自定义 AdapterRegistry 的包名，如果是 Android 项目，推荐直接使用 "android.namespace"
    arg("moshi-companion.generateAdapterRegistryPackageName", "com.yourdomain.yourpackage")
    // 自定义 AdapterRegistry 的类名
    arg("moshi-companion.generateAdapterRegistryClassName", "YourCustomMoshiAdapterRegistry")
}
```

形如：

```
com.yourdomain.yourpackage.generated.YourCustomMoshiAdapterRegistry
```

如果你会维护一个专注于数据模型的模块化项目，我们建议像上述示例这样固定生成的 `AdapterRegistry` 包名和类名，防止自动生成的内容不符合你的项目需求。

如果你需要生成仅用于当前项目可访问的 `AdapterRegistry`，可以通过添加如下 KSP 参数来实现：

```kotlin
ksp {
    arg("moshi-companion.generateAdapterRegistryRestrictedAccess", "true")
}
```

这样，生成的 `AdapterRegistry` 类将被设置为 `internal`，只能在当前模块中访问，从而避免被其它项目误用或滥用。

Moshi Companion 自动生成的混淆规则包含了对 Enum 类的键值混淆保护功能，默认情况下仅对类名进行混淆。

请注意 Moshi 的默认行为是对 `@JsonClass(generateAdapter = false)` 注解的 Enum 类才不会进行混淆，在使用 Moshi Companion 后，所有 Enum 类均会被保护不被混淆。

如果你不需要这个功能，可以通过添加如下 KSP 参数来关闭 (不建议关闭)：

```kotlin
ksp {
    arg("moshi-companion.proguardRulesKeepEnumClasses", "false")
}
```

如果你不希望 Moshi Companion 自动生成任何混淆规则，也可以通过添加如下 KSP 参数来关闭 (不建议关闭)：

```kotlin
ksp {
    arg("moshi-companion.generateProguardRules", "false")
}
```

## 扩展 API

Moshi Companion 提供了 `TypeRef` 类来简化 `Types.newParameterizedType` 的使用，它的启发和实践来自老牌 [Gson](https://github.com/google/gson) 项目的 `TypeToken`，你可以通过继承 `TypeRef` 来创建一个类型引用，然后通过 `type` 属性获取 `Type` 对象。

```kotlin
val typeRef = typeRef<List<YourDataClass>>()
// 获取 Type 对象，即 List<YourDataClass>
val type = typeRef.type
// 获取原始对象，即 List
val rawType = typeRef.rawType
```

你可以直接将获取到的 `type` 对象传递给 `Moshi.adapter` 方法来获取对应的 `JsonAdapter`。

```kotlin
val adapter = moshi.adapter<List<YourDataClass>>(typeRef.type)
```

当然，你也可以不需要写的这么复杂，你可以直接使用 Moshi Companion 提供的 `typeAdapter` 扩展函数来简化这个过程：

```kotlin
val adapter = moshi.typeAdapter<List<YourDataClass>>()
// 对比原版写法
val type = Types.newParameterizedType(List::class.java, YourDataClass::class.java)
val adapter = moshi.adapter<List<YourDataClass>>(type)
```

`TypeRef` 已经在 Moshi Companion 默认生成的混淆规则中被处理，完全不受 R8 的混淆和优化影响，同样地，泛型类的类名可以完全做到安全混淆、压缩体积、减少反射和暴露风险。

## 故障排查

如果你没有禁用 Moshi Companion 的混淆规则生成，但是混淆规则没有被加入到 `shrink-rules`，你可以在 R8 结束后检查生成的 `configuration.txt` 文件，查看是否包含 "JsonAdapter"。

目前在 Android 项目中这个问题可能出现在项目主模块 (例如 "app")，如果混淆规则没生效，请查看 `build/generated/ksp/release/resources/META-INF/proguard/` 下有没有规则文件，如果存在，那么请在 `build.gradle.kts` 的 `buildTypes` 中添加如下配置：

```kotlin
release {
    isMinifyEnabled = true

    proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    // 指定 Moshi Companion 生成的混淆规则文件
    file("build/generated/ksp/release/resources/META-INF/proguard/").listFiles()?.firstOrNull()?.let {
        proguardFiles(it)
    }
}
```