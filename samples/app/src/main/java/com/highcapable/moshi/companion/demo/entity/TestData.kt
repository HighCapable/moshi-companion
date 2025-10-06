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
 * This file is created by fankes on 2025/10/7.
 */
package com.highcapable.moshi.companion.demo.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ResponseData<T>(
    val code: Int,
    val message: String,
    val data: T?
)

@JsonClass(generateAdapter = true)
data class MyFood(
    override var type: FoodType = FoodType.Meat
) : Food<MyFood.Info>() {

    @JsonClass(generateAdapter = true)
    data class Info(
        val name: String,
        val desc: String,
        val number: List<Int>
    )
}

abstract class Food<T : Any> {

    abstract var type: FoodType

    var data: T? = null
}

enum class FoodType {
    Fruit,
    Vegetable,
    Meat
}