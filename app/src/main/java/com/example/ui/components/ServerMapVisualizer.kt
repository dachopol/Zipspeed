package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.ServerInfo
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen

@Composable
fun ServerMapVisualizer(
    selectedServer: ServerInfo,
    language: Language,
    onSelectServer: (ServerInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val isTh = language == Language.TH

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberPanel, RoundedCornerShape(20.dp))
            .border(1.dp, Color(0x282F7BFF), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("server_map_visualizer"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color(0x222F7BFF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Public, contentDescription = null, tint = NeonBlue)
            }
            Spacer(Modifier.size(10.dp))
            Column {
                Text(
                    text = if (isTh) "เส้นทาง Anycast จริง" else "Real Anycast Routing",
                    color = CyberInk,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isTh) "ไม่แสดงตำแหน่งสมมติบนแผนที่" else "No simulated geographic positions",
                    color = CyberMuted,
                    fontSize = 11.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x12FFFFFF), RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Dns, contentDescription = null, tint = NeonGreen)
            Column {
                Text(selectedServer.name, color = CyberInk, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                Text(selectedServer.subLocation, color = CyberMuted, fontSize = 11.sp)
            }
        }

        Text(
            text = if (isTh)
                "PoP/เมืองจริงจะแสดงเมื่อ endpoint ส่งข้อมูลที่ตรวจสอบได้ระหว่างการทดสอบ"
            else
                "The actual PoP/city is shown only when the endpoint returns verifiable metadata during a test.",
            color = CyberMuted,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )

        Spacer(Modifier.height(2.dp))
    }
}
