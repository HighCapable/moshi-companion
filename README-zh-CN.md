# Moshi Companion

[![GitHub license](https://img.shields.io/github/license/HighCapable/moshi-companion?color=blue&style=flat-square)](https://github.com/HighCapable/moshi-companion/blob/main/LICENSE)
[![Telegram](https://img.shields.io/badge/discussion-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/HighCapable)
[![Telegram](https://img.shields.io/badge/discussion%20dev-Telegram-blue.svg?logo=telegram&style=flat-square)](https://t.me/HighCapable_Dev)
[![QQ](https://img.shields.io/badge/discussion%20dev-QQ-blue.svg?logo=tencent-qq&logoColor=red&style=flat-square)](https://qm.qq.com/cgi-bin/qm/qr?k=Pnsc5RY6N2mBKFjOLPiYldbAbprAU3V7&jump_from=webapi&authKey=X5EsOVzLXt1dRunge8ryTxDRrh9/IiW1Pua75eDLh9RE3KXE+bwXIYF5cWri/9lf)

为 Moshi 提供更多实用功能的伴侣。

[English](README.md) | 简体中文

| <img src="https://github.com/HighCapable/.github/blob/main/img-src/logo.jpg?raw=true" width = "30" height = "30" alt="LOGO"/> | [HighCapable](https://github.com/HighCapable) |
|-------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------|

这个项目属于上述组织，**点击上方链接关注这个组织**，发现更多好项目。

## 这是什么

这是一个针对 [Moshi](https://github.com/square/moshi) 项目进行功能性优化的工具，使 Moshi 能够在 Android 项目启用 R8 后，不再依赖于
`Class.forName` 寻找 `moshi-kotlin-codegen` 生成的适配器类，从而实现对实体类的名称、字段的完全混淆，提升代码安全性和降低 APK 体积。

## 项目动机

在 Android 项目中使用 Moshi 进行 JSON 序列化和反序列化时，在使用 `@JsonClass(generateAdapter = true)` 注解的实体类时，Moshi 会通过 `Class.forName`
方法动态加载 `moshi-codegen` 生成的适配器类。如果项目启用了 R8 进行代码混淆和优化，Moshi 默认的混淆规则是保留实体类和适配器类的名称不被混淆，这样能够正确反射，但是会导致代码安全性降低和
APK 体积增大。

于是我对 Moshi 的适配器生成原理进行了探索，我很认可这种生成手写代码的高性能解决方案。通过研究，我认为将 `moshi-kotlin-codegen` 生成的适配器手动注册到
Moshi
中来使得类名能够混淆是一个可行的解决方案，于是我曾作为这个想法向 Moshi 项目提出了 [PR](https://github.com/square/moshi/pull/2002)
，但是不得不承认修改项目本身可能会造成一些不必要的维护问题，不一定符合所有人的需求，而且有一些代码生成的性能问题需要解决，本着不对项目本身进行侵入的原则，我现在选择将这个想法独立出来，作为一个单独的项目进行维护。

所以正如我的 PR 提出的方案一样，目前的实现方案是通过读取整个项目的 `@JsonClass(generateAdapter = true)` 注解，获取所有需要生成适配器的实体类并创建
`AdapterRegistry`，然后在运行时通过 `Moshi.Builder` 手动注册这些适配器，并且改进了混淆规则的生成。

## 开始使用

- [点击这里](docs/guide-zh-CN.md) 查看使用文档

## 更新日志

- [点击这里](docs/changelog-zh-CN.md) 查看历史更新日志

## 项目推广

<!--suppress HtmlDeprecatedAttribute -->
<div align="center">
    <h2>嘿，还请君留步！👋</h2>
    <h3>这里有 Android 开发工具、UI 设计、Gradle 插件、Xposed 模块和实用软件等相关项目。</h3>
    <h3>如果下方的项目能为你提供帮助，不妨为我点个 star 吧！</h3>
    <h3>所有项目免费、开源，遵循对应开源许可协议。</h3>
    <h1><a href="https://github.com/fankes/fankes/blob/main/project-promote/README-zh-CN.md">→ 查看更多关于我的项目，请点击这里 ←</a></h1>
</div>

## Star History

![Star History Chart](https://api.star-history.com/svg?repos=HighCapable/moshi-companion&type=Date)

## 第三方开源使用声明

- [Moshi](https://github.com/square/moshi)
- [Gson](https://github.com/google/gson)

## 许可证

- [Apache-2.0](https://www.apache.org/licenses/LICENSE-2.0)

```
Apache License Version 2.0

Copyright (C) 2019 HighCapable

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

版权所有 © 2019 HighCapable