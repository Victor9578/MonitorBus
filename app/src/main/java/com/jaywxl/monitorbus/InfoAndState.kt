package com.jaywxl.monitorbus

data class BusInfo(
    val busname: String,
    val stationname: String,
    val trip: List<Trip>
)

data class Trip(
    val arrival: String,
    val station_left: String
)

// 定义 BusInfoState 用于管理加载状态
sealed class BusInfoState {
    data object Loading : BusInfoState()
    data class Success(val busInfoList: List<BusInfo>) : BusInfoState()
    data object Error : BusInfoState()
}
