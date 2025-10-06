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
package com.highcapable.moshi.companion.demo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.highcapable.moshi.companion.api.addRegistry
import com.highcapable.moshi.companion.api.typeAdapter
import com.highcapable.moshi.companion.demo.entity.FoodType
import com.highcapable.moshi.companion.demo.entity.MyFood
import com.highcapable.moshi.companion.demo.entity.ResponseData
import com.highcapable.moshi.companion.demo.generated.SampleAdapterRegistry
import com.squareup.moshi.Moshi

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        testData()
    }

    private fun testData() {
        val moshi = Moshi.Builder()
            .addRegistry(SampleAdapterRegistry())
            .build()

        val responseData = ResponseData(
            code = 200,
            message = "Success",
            data = listOf(
                "Test1",
                "Test2",
                "Test3"
            )
        )

        val respAdapter = moshi.typeAdapter<ResponseData<List<String>>>()
        val respJson = respAdapter.indent("  ").toJson(responseData)

        val fromRespJson = """
            {
              "code": 200,
              "message": "Success",
              "data": [
                "Test1",
                "Test2",
                "Test3"
              ]
            }
        """.trimIndent()
        val fromRespData = respAdapter.fromJson(fromRespJson)

        val myFood = MyFood(
            type = FoodType.Fruit,
        ).apply {
            data = MyFood.Info(
                name = "Apple",
                desc = "A sweet red fruit",
                number = listOf(1, 2, 3)
            )
        }

        val foodAdapter = moshi.typeAdapter<MyFood>()
        val foodJson = foodAdapter.indent("  ").toJson(myFood)

        val fromFoodJson = """
            {
              "type": "Fruit",
              "data": {
                "name": "Apple",
                "desc": "A sweet red fruit",
                "number": [1, 2, 3]
              }
            }
        """.trimIndent()
        val fromFoodData = foodAdapter.fromJson(fromFoodJson)

        MaterialAlertDialogBuilder(this)
            .setTitle("Moshi Companion Test Data")
            .setMessage(
                "ResponseData to JSON:\n$respJson\n\n" +
                    "ResponseData from JSON:\n$fromRespData\n\n" +
                    "MyFood to JSON:\n$foodJson\n\n" +
                    "MyFood from JSON:\n$fromFoodData"
            )
            .setPositiveButton("OK") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }
}