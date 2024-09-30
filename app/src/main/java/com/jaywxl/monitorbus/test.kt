package com.jaywxl.monitorbus

import com.jaywxl.monitorbus.api.apiBus


fun main() {
    // 模拟 favoriteLine 列表，包含公交线路和站点
    val favoriteLines = listOf(
        listOf("900000153866", "900000153866017"),  // 第一组线路与站点
        listOf("610100010931", "610100010931017"),  // 第二组线路与站点
        listOf("610100010884","610100010884044")
    )

//    for (favoritLine in favoriteLines) {
//        println(favoritLine)
//        println(ApiBus(favoritLine[0],favoritLine[1]))
//
//    }

//     将 favoriteLine 中的线路和站点遍历，并请求数据
    val busInfoList = favoriteLines.map { (line, station) ->
        apiBus(line, station)  // 请求每个线路和站点对应的公交信息
    }
    println(busInfoList)
}