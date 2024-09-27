package com.jaywxl.monitorbus.api

import com.google.gson.JsonArray
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import okhttp3.Response


fun ApiBus(lines: String, stations: String): JsonObject {
    // 定义 key、lines 和 stations
    val key = "99cbc5f7101811806207794bb914ccad"

    // 发起 HTTP 请求
    val busInfo = getBusInfo(key, lines, stations)
    val busTripInfo = getBusTripInfo(key, lines, stations)
//    println(busInfo)
//    println(busTripInfo)

    // 创建新的 JSON 对象
    val mergedJson = JsonObject().apply {
        addProperty("busname", busInfo?.get("busname")?.asString) // 添加 busname
        addProperty("stationname", busInfo?.get("stationname")?.asString) // 添加 stationname
        add("trip", busTripInfo?.getAsJsonArray("trip")) // 添加 trip 数组
    }

    return mergedJson
}

fun request(url: String): Response? {
    val client = OkHttpClient()
    val request = Request.Builder()
        .url(url)
//        .addHeader("User-Agent","Mozilla/5.0 (Linux; Android 13; XQ-BC72 Build/61.2.A.0.472A; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/116.0.0.0 Mobile Safari/537.36 XWEB/1160117 MMWEBSDK/20240501 MMWEBID/8445 MicroMessenger/8.0.49.2685(0x28003145) WeChat/arm64 Weixin GPVersion/1 NetType/4G Language/en ABI/arm64 MiniProgramEnv/android")
//        .addHeader("Accept-Encoding","gzip,compress,br,deflate")
        .addHeader("charset", "utf-8")
        .addHeader("content-type", "application/json")
        .addHeader("Referer", "https://servicewechat.com/wxf3edeca73e7ba2ff/6/page-frame.html")
        .build()
    return client.newCall(request).execute()
}

fun getBusTripInfo(key: String, lines: String, stations: String): JsonObject? {
    // 构建 URL
    val url = "https://aisle.amap.com/ws/mapapi/realtimebus/linestation" +
            "?key=$key" +
            "&lines=$lines" +
            "&stations=$stations" +
            "&need_bus_status=1"

    request(url)?.use { response ->
        if (!response.isSuccessful) throw Exception("Unexpected code $response")

        val jsonData = response.body?.string() ?: return null
        val jsonObject = JsonParser.parseString(jsonData).asJsonObject

        // 提取 buses 数组
        val busesArray = jsonObject.getAsJsonArray("buses")

        if (busesArray.size() > 0) {
            // 获取第一个公交车对象
            val busObject = busesArray[0].asJsonObject

            // 从公交车对象中提取 trip 数组
            val tripArray = busObject.getAsJsonArray("trip")

            // 构建最终返回的 trip JSON
            val tripJson = JsonArray()

            if (tripArray == null || tripArray.size() == 0) {
                // trip 为 null，构建一个返回 sub_text 的单一 JSON 对象
                val tripObj1 = JsonObject()
                tripObj1.addProperty("arrival", "等候发车")
                tripObj1.addProperty("station_left", "")

                val tripObj2 = JsonObject()
                tripObj2.addProperty("arrival", "")
                tripObj2.addProperty("station_left", "")

                tripJson.add(tripObj1)
                tripJson.add(tripObj2)
            } else if (tripArray.size() == 1) {
                // trip 长度为 1，构建两个对象，第一个为正常的 trip 数据，第二个为等候发车
                val firstTrip = tripArray[0].asJsonObject
                val tripObj1 = JsonObject()
                tripObj1.addProperty(
                    "arrival",
                    (firstTrip.get("arrival").asInt / 60).toString() + "min"
                )
                tripObj1.addProperty(
                    "station_left",
                    (firstTrip.get("station_left").asInt + 1).toString() + "站"
                )

                val tripObj2 = JsonObject()
                tripObj2.addProperty("arrival", "等候发车")
                tripObj2.addProperty("station_left", "")

                tripJson.add(tripObj1)
                tripJson.add(tripObj2)
            } else {
                // trip 长度大于等于 2，返回前两个对象
                for (i in 0..1) {
                    val tripObj = JsonObject()
                    val currentTrip = tripArray[i].asJsonObject
                    tripObj.addProperty(
                        "arrival",
                        (currentTrip.get("arrival").asInt / 60).toString() + "min"
                    )
                    tripObj.addProperty(
                        "station_left",
                        (currentTrip.get("station_left").asInt + 1).toString() + "站"
                    )
                    tripJson.add(tripObj)
                }
            }

            // 最终返回 tripJson
            val result = JsonObject()
            result.add("trip", tripJson)

            return result
        }

    }
    return null
}

fun getBusInfo(key: String, lines: String, stations: String): JsonObject? {
    val url = "https://aisle.amap.com/ws/mapapi/poi/newbus" +
            "?key=$key" +
            "&id=$lines"

    request(url)?.use { response ->
        if (!response.isSuccessful) throw Exception("Unexpected code $response")

        val jsonData = response.body?.string() ?: return null
        val jsonObject = JsonParser.parseString(jsonData).asJsonObject

        val busName = jsonObject.getAsJsonArray("busline_list")[0].asJsonObject.get("name").asString

        val busStations =
            jsonObject.getAsJsonArray("busline_list")[0].asJsonObject.getAsJsonArray("stations")
        var stationName: String? = null
        for (station in busStations) {
            val stationJson = station.asJsonObject
            if (stationJson.get("station_id").asString == stations) {
                stationName = stationJson.get("name").asString
                break
            }
        }

        val result = JsonObject().apply {
            addProperty("busname", busName)
            addProperty("stationname", stationName)
        }

        return result
    }
    return null
}