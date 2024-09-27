package com.jaywxl.monitorbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.jaywxl.monitorbus.api.ApiBus
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            MyApp()
        }
    }
}

@Composable
fun MyApp() {
    MaterialTheme(
        colors = darkColors(
            primary = Color.White, // 主色调
            onPrimary = Color.Black, // 主要内容颜色
            background = Color.Black, // 背景颜色
            onBackground = Color.White, // 背景内容颜色
            surface = Color.Black, // 表面颜色
            onSurface = Color.White // 表面内容颜色
        )
    )
    {
        Scaffold(
            topBar = {
                // 如果需要，可以添加顶部应用栏
//                 TopAppBar (title = )
            },
            content = { paddingValues ->
                HorizontalPager(
                    state = rememberPagerState(initialPage = 1) { 3 },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .focusable()
                ) { page ->
                    when (page) {
                        0 -> AppInfoPage()
                        1 -> FavoriteLinesPage()
                        2 -> InputLinePage()
                    }
                }
            }
        )
    }
}

@Composable
fun AppInfoPage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "MonitorBus", style = MaterialTheme.typography.h6, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "特点地点、特定线路，通过手表查看实时公交信息。",
            style = MaterialTheme.typography.body2
        )
        val authorText = "Author："
        val contributorText = "Contributor："

        // 设置相同的宽度来对齐冒号
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = authorText,
                style = MaterialTheme.typography.body2.copy(
                    textAlign = TextAlign.Right,
                    fontSize = 10.sp
                ),
                modifier = Modifier.width(100.dp)
            )
            Text(text = "@Jw", style = MaterialTheme.typography.body2.copy(fontSize = 10.sp))
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = contributorText,
                style = MaterialTheme.typography.body2.copy(
                    textAlign = TextAlign.Right,
                    fontSize = 10.sp
                ),
                modifier = Modifier.width(100.dp)
            )
            Text(text = "@meanboi", style = MaterialTheme.typography.body2.copy(fontSize = 10.sp))
        }
    }
}

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
    object Loading : BusInfoState()
    data class Success(val busInfoList: List<BusInfo>) : BusInfoState()
    object Error : BusInfoState()
}

@Composable
fun FavoriteLinesPage() {
    // 模拟 favoriteLine 列表，包含公交线路和站点
    val favoriteLine = listOf(
        listOf("900000153866", "900000153866017"),  // 111-公园南路延兴门东路口
        listOf("610100010932", "610100010932035"),  // 25-华商集团
        listOf("610100011127", "610100011127027"),  // 48-华商集团
        listOf("900000037380", "900000037380003")   // 269-华商集团
    )

    // 用于存储 UI 的加载状态
    var busInfoState by remember { mutableStateOf<BusInfoState>(BusInfoState.Loading) }

    // 异步加载数据
    LaunchedEffect(Unit) {
        try {
            // 异步请求每个线路和站点的公交信息
            val busInfoList = favoriteLine.mapNotNull { (line, station) ->
                val jsonObject = withContext(Dispatchers.IO) {
                    ApiBus(line, station) // 请求每个线路和站点对应的公交信息
                }

                // 将 JsonObject 转换为 BusInfo
                jsonObject.let {
                    BusInfo(
                        busname = it.get("busname").asString,
                        stationname = it.get("stationname").asString,
                        trip = Gson().fromJson(it.getAsJsonArray("trip"), Array<Trip>::class.java)
                            .toList()
                    )
                }
            }
            // 更新状态为成功加载
            busInfoState = BusInfoState.Success(busInfoList)
        } catch (e: Exception) {
            // 更新状态为错误
            busInfoState = BusInfoState.Error
        }
    }

    // 根据状态显示 UI
    when (busInfoState) {
        is BusInfoState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Loading...", style = MaterialTheme.typography.body2)
            }
        }

        is BusInfoState.Success -> {
            val busInfoList = (busInfoState as BusInfoState.Success).busInfoList

            LazyColumn(
                modifier = Modifier
                    .focusable()
                    .fillMaxHeight()
            ) {
                items(busInfoList) { busInfo ->
                    Box(
                        modifier = Modifier
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // 显示每个公交信息
                        BusInfoItem(busInfo)
                    }
                }
            }
        }

        is BusInfoState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Failed to load data",
                    style = MaterialTheme.typography.body2,
                    color = Color.Red
                )
            }
        }
    }
}

@Composable
fun BusInfoItem(busInfo: BusInfo) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(text = "${busInfo.busname}", style = MaterialTheme.typography.h6)
        Text(text = "${busInfo.stationname}", style = MaterialTheme.typography.body2)

        // 分割线
        Spacer(modifier = Modifier.height(2.dp)) // 调整与分割线之间的间距
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color.Gray) // 设置分割线颜色
        )
        Spacer(modifier = Modifier.height(4.dp)) // 调整与分割线之间的间距

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .height(40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                busInfo.trip.forEachIndexed { index, trip ->
                    // 显示到达时间和剩余站点
                    Text(
                        text = "${trip.arrival}",
                        style = MaterialTheme.typography.body2.copy(fontSize = 12.sp),
                        modifier = Modifier
                            .padding(bottom = 10.dp)
                            .width(55.dp)
                    )
                    Text(
                        text = "${trip.station_left}",
                        style = MaterialTheme.typography.body2.copy(fontSize = 10.sp),
                        modifier = Modifier.padding(top = 10.dp).width(25.dp)
                    )
                    // 如果不是最后一个项，添加竖杠
                    if (index == 0 && trip.arrival != "等候发车") {
                        Divider(
                            color = Color.DarkGray, // 可以根据需要更改颜色
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(6.dp) // 设置竖杠的宽度
                                .padding(all = 2.dp) // 添加一些水平间距
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun InputLinePage() {
    // 这里我们使用上面生成的示例数据
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(30.dp)
            .clip(RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.spacedBy(8.dp) // 设置每个格子之间的间距
    ) {
        Text(text = "添加线路", style = MaterialTheme.typography.h6)
    }
}

//@Preview(device = Devices, showSystemUi = true)
//
//
//@Composable
//fun DefaultPreview() {
//}