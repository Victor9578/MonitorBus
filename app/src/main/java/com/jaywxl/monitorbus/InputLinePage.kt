package com.jaywxl.monitorbus

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch

import com.jaywxl.monitorbus.api.request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

@Composable
fun InputLinePage() {
    // 状态用于保存用户输入的城市和关键词
    var selectedCity by remember { mutableStateOf("610100") } // 默认城市代码
    var keywords by remember { mutableStateOf("") } // 用户输入的关键词
    val cityOptions = listOf("610100", "110000", "310000", "440100") // 城市选择列表
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
    ) {
        Text(text = "添加线路", style = MaterialTheme.typography.h6)

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 城市选择滚轮
            Text(text = "城市", style = MaterialTheme.typography.body2.copy(fontSize = 12.sp))
            ScrollPicker(selectedCity = selectedCity,
                cityOptions = cityOptions,
                onCitySelected = { city -> selectedCity = city })
        }

        Divider(
            color = Color.Gray, // 可以根据需要更改颜色
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp) // 设置竖杠的宽度
                .padding(all = 2.dp) // 添加一些水平间距
        )

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "线路", style = MaterialTheme.typography.body2.copy(fontSize = 12.sp))
            BasicTextField(
                value = keywords,
                onValueChange = { keywords = it },
                textStyle = TextStyle(
                    fontSize = 12.sp, // 控制字体大小
                    color = Color.White // 字体颜色
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp) // 限制高度
                    .border(1.dp, Color.Black)
                    .wrapContentSize(),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.background(Color.Red),
                        contentAlignment = Alignment.Center // 设置内容靠左居中
                    ) {
                        innerTextField() // 显示输入框内容
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )
        }


        // 提交按钮，用于生成URL
        Button(onClick = {
            val lineInfo = buildUrl(selectedCity, keywords)
        }) {
            Text(text = "生成URL")
        }
    }
}

@Composable
fun ScrollPicker(selectedCity: String, cityOptions: List<String>, onCitySelected: (String) -> Unit) {
    val scrollState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier.height(30.dp) // 控制滚动区域的高度
//            .padding(2.dp)
//            .background(Color.LightGray)
    ) {
        LazyColumn(
            state = scrollState,
            contentPadding = PaddingValues(vertical = 8.dp), // 增加上下的可见空间
            verticalArrangement = Arrangement.Center // 居中对齐
        ) {
            items(cityOptions.size) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp), // 每个条目的高度
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cityOptions[index], style = if (selectedCity == cityOptions[index]) {
                            MaterialTheme.typography.body2.copy(fontSize = 12.sp)
                        } else {
                            MaterialTheme.typography.body2.copy(fontSize = 8.sp)
                        }
                    )
                }
            }
        }
        // 使用 LaunchedEffect 来监听滚动，并进行选择
        LaunchedEffect(scrollState) {
            snapshotFlow { scrollState.firstVisibleItemIndex }.collect { index ->
                if (index in cityOptions.indices) {
                    onCitySelected(cityOptions[index])
                }
            }
        }
    }
}

fun buildUrl(city: String, keywords: String): Any? {
    val inputUrl =
        "https://restapi.amap.com/v3/assistant/inputtips?key=99cbc5f7101811806207794bb914ccad&keywords=${keywords}路&city=${city}&datatype=busline&citylimit=true"

    // 启动协程
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = request(inputUrl)
            val lineInfo = response?.use {
                if (!it.isSuccessful) throw Exception("Unexpected code $response")

                val jsonData = it.body?.string() ?: return@use null

                // 解析第一个请求的 JSON 数据
                val gson = Gson()
                val jsonObject = gson.fromJson(jsonData, JsonObject::class.java)
                val tipsArray = jsonObject.getAsJsonArray("tips")
                if (tipsArray != null && tipsArray.size() > 0) {
                    val firstTip = tipsArray[0].asJsonObject
                    val busId = firstTip.get("id").asString

                    // 构建第二个 URL
                    val busInfoUrl =
                        "https://aisle.amap.com/ws/mapapi/poi/newbus?key=99cbc5f7101811806207794bb914ccad&id=$busId"

                    // 请求新的 URL 获取公交线路信息
                    val busInfoResponse = request(busInfoUrl)
                    busInfoResponse?.use { busResponse ->
                        if (!busResponse.isSuccessful) throw Exception("Unexpected code $busResponse")

                        val busInfoJson = busResponse.body?.string() ?: return@use null

                        // 解析第二个请求的 JSON 数据
                        val busJsonObject = gson.fromJson(busInfoJson, JsonObject::class.java)
                        val buslineList = busJsonObject.getAsJsonArray("busline_list")
                        if (buslineList != null && buslineList.size() > 0) {
                            val busline = buslineList[0].asJsonObject
                            val busName = busline.get("name").asString

                            // 提取站点信息
                            val busStations = busline.getAsJsonArray("stations")
                            for (station in busStations) {
                                val stationJson = station.asJsonObject
                                val stationId = stationJson.get("station_id").asString
                                val stationName = stationJson.get("name").asString
                                println("Station ID: $stationId, Station Name: $stationName")
                            }

                            println("Bus Name: $busName")
                        }
                    }
                }
                else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return null
}

