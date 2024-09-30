package com.jaywxl.monitorbus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.darkColors
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


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
                    state = rememberPagerState(initialPage = 2) { 3 },
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

