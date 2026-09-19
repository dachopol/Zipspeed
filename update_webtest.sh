sed -i '332,$c\
// -------------------------------------------------------------\
// 2. WEB BROWSING & CDN PERFORMANCE VIEW\
// -------------------------------------------------------------\
@Composable\
fun WebTestView(\
    webState: WebTestState,\
    language: Language,\
    onStartTest: () -> Unit,\
    modifier: Modifier = Modifier\
) {\
    Column(\
        modifier = modifier\
            .fillMaxWidth()\
            .testTag("web_test_view"),\
        verticalArrangement = Arrangement.spacedBy(14.dp)\
    ) {\
        // Overall Web Browsing Score Card\
        Box(\
            modifier = Modifier\
                .fillMaxWidth()\
                .clip(RoundedCornerShape(18.dp))\
                .background(\
                    Brush.verticalGradient(\
                        listOf(Color(0xFF14202E), Color(0xFF0F151E))\
                    )\
                )\
                .border(1.dp, NeonBlue.copy(alpha = 0.35f), RoundedCornerShape(18.dp))\
                .padding(18.dp)\
        ) {\
            Row(\
                modifier = Modifier.fillMaxWidth(),\
                verticalAlignment = Alignment.CenterVertically,\
                horizontalArrangement = Arrangement.SpaceBetween\
            ) {\
                Column {\
                    Text(\
                        text = "คะแนนประสบการณ์ท่องเว็บ",\
                        color = Color(0xB3FFFFFF),\
                        fontSize = 11.sp,\
                        fontWeight = FontWeight.ExtraBold,\
                        letterSpacing = 0.8.sp\
                    )\
                    Spacer(modifier = Modifier.height(6.dp))\
                    Text(\
                        text = if (webState.isCompleted) "${webState.overallScore}/100" else "--/100",\
                        color = if (webState.isCompleted) NeonGreen else Color.White,\
                        fontSize = 32.sp,\
                        fontWeight = FontWeight.Black\
                    )\
                    Text(\
                        text = if (webState.isCompleted)\
                            (if (webState.overallScore >= 90) "เกรด A+ (โหลดเร็วเป็นพิเศษ)"\
                            else if (webState.overallScore >= 80) "เกรด A (เร็วปกติ)"\
                            else if (webState.overallScore >= 60) "เกรด B (ใช้งานได้)"\
                            else "เกรด C (ควรปรับปรุง)")\
                        else\
                            "พร้อมทดสอบความเร็วเปิดหน้าเว็บ",\
                        color = Color(0xB3FFFFFF),\
                        fontSize = 12.sp\
                    )\
                }\
                Button(\
                    onClick = onStartTest,\
                    enabled = !webState.isTesting,\
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue, disabledContainerColor = NeonBlue.copy(alpha = 0.5f)),\
                    shape = RoundedCornerShape(12.dp),\
                    modifier = Modifier.testTag("btn_start_web_test")\
                ) {\
                    Icon(Icons.Default.Language, contentDescription = null, tint = Color.White)\
                    Spacer(modifier = Modifier.width(6.dp))\
                    Text(\
                        text = if (webState.isTesting) "กำลังวัด..." else "เริ่มทดสอบ",\
                        color = Color.White,\
                        fontWeight = FontWeight.Bold,\
                        fontSize = 12.sp\
                    )\
                }\
            }\
        }\
\
        // List of websites tested\
        Text(\
            text = "จำลองการโหลดเว็บไซต์จริง (HTTP Latency)",\
            color = Color(0xB3FFFFFF),\
            fontSize = 12.sp,\
            fontWeight = FontWeight.SemiBold\
        )\
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {\
            webState.sites.forEach { site ->\
                Row(\
                    modifier = Modifier\
                        .fillMaxWidth()\
                        .clip(RoundedCornerShape(12.dp))\
                        .background(Color(0xFF161B22))\
                        .border(1.dp, Color(0x18FFFFFF), RoundedCornerShape(12.dp))\
                        .padding(horizontal = 14.dp, vertical = 12.dp),\
                    verticalAlignment = Alignment.CenterVertically,\
                    horizontalArrangement = Arrangement.SpaceBetween\
                ) {\
                    Column {\
                        Text(\
                            text = site.name,\
                            color = Color.White,\
                            fontSize = 14.sp,\
                            fontWeight = FontWeight.Bold\
                        )\
                        Text(\
                            text = "${site.category} (${site.domain})",\
                            color = Color(0xB3FFFFFF),\
                            fontSize = 11.sp\
                        )\
                    }\
                    Row(\
                        verticalAlignment = Alignment.CenterVertically,\
                        horizontalArrangement = Arrangement.spacedBy(8.dp)\
                    ) {\
                        if (site.latencyMs > 0) {\
                            Column(horizontalAlignment = Alignment.End) {\
                                Text(\
                                    text = "${site.latencyMs} ms",\
                                    color = if (site.latencyMs < 80) NeonGreen else if (site.latencyMs < 200) GoldPro else NeonAmber,\
                                    fontSize = 14.sp,\
                                    fontWeight = FontWeight.ExtraBold\
                                )\
                                Text(\
                                    text = "สถานะ: ${site.statusText}",\
                                    color = Color(0xB3FFFFFF),\
                                    fontSize = 10.sp\
                                )\
                            }\
                        } else if (site.latencyMs == -1) {\
                            Column(horizontalAlignment = Alignment.End) {\
                                Text(\
                                    text = "Failed",\
                                    color = NeonRose,\
                                    fontSize = 14.sp,\
                                    fontWeight = FontWeight.ExtraBold\
                                )\
                                Text(\
                                    text = site.statusText,\
                                    color = Color(0xB3FFFFFF),\
                                    fontSize = 10.sp\
                                )\
                            }\
                        } else {\
                            Text(\
                                text = site.statusText,\
                                color = Color(0xB3FFFFFF),\
                                fontSize = 12.sp\
                            )\
                        }\
                    }\
                }\
            }\
        }\
    }\
}\
' app/src/main/java/com/example/ui/components/SubTestsComponents.kt
bash update_webtest.sh
