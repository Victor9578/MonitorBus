package com.jaywxl.monitorbus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
