package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.ui.components.verticalScrollbar
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SpeedTestRecord
import com.example.model.Language
import com.example.model.SpeedUnit
import com.example.ui.components.ShareDetailModal
import com.example.ui.components.ShareReportData
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.CyberPanel
import com.example.ui.theme.NeonBlue
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPurple
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<SpeedTestRecord>,
    speedUnit: SpeedUnit,
    language: Language,
    onDeleteRecord: (SpeedTestRecord) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
    var showExportModal by remember { mutableStateOf(false) }
    var selectedRecordForShare by remember { mutableStateOf<SpeedTestRecord?>(null) }

    fun shareExportData(content: String, mimeType: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, content)
            type = mimeType
        }
        val chooser = Intent.createChooser(
            sendIntent,
            if (language == Language.TH) "ส่งออกประวัติการทดสอบ Zipspeed" else "Export Zipspeed Test History"
        )
        context.startActivity(chooser)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .testTag("history_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == Language.TH) "ประวัติการทดสอบ" else "TEST HISTORY",
                color = CyberInk,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("history_title")
            )

            if (records.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { showExportModal = true },
                        modifier = Modifier.testTag("export_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Export History",
                            tint = NeonBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = if (language == Language.TH) "ส่งออก" else "EXPORT",
                            color = NeonBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(
                        onClick = onClearAll,
                        modifier = Modifier.testTag("clear_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Clear All",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = if (language == Language.TH) "ล้าง" else "CLEAR",
                            color = Color(0xFFFF5252),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (records.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0x1A2F7BFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = NeonBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (language == Language.TH) "ยังไม่มีประวัติการทดสอบ" else "No Test History Yet",
                        color = CyberInk,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (language == Language.TH)
                            "กดปุ่ม 'เริ่มทดสอบความเร็ว' เพื่อบันทึกผลการวัดผลครั้งแรก"
                        else
                            "Tap 'START SPEED TEST' to save your first measurement.",
                        color = CyberMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        } else {
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScrollbar(listState),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(records, key = { it.id }) { record ->
                    HistoryCardItem(
                        record = record,
                        speedUnit = speedUnit,
                        dateFormat = dateFormat,
                        language = language,
                        onShare = { selectedRecordForShare = record },
                        onDelete = { onDeleteRecord(record) }
                    )
                }
            }
        }

        if (showExportModal) {
            ExportOptionsModal(
                language = language,
                onDismiss = { showExportModal = false },
                onExportText = {
                    val textSummary = buildTextSummary(records, speedUnit, language)
                    shareExportData(textSummary, "text/plain")
                    showExportModal = false
                },
                onExportJson = {
                    val jsonExport = buildJsonExport(records)
                    shareExportData(jsonExport, "application/json")
                    showExportModal = false
                }
            )
        }

        selectedRecordForShare?.let { r ->
            ShareDetailModal(
                reportData = ShareReportData(
                    downloadMbps = r.downloadMbps,
                    uploadMbps = r.uploadMbps,
                    pingMs = r.pingMs,
                    jitterMs = r.jitterMs,
                    packetLossPercent = 0.0,
                    serverName = r.serverName,
                    networkType = r.networkType,
                    timestamp = r.timestamp
                ),
                speedUnit = speedUnit,
                language = language,
                onDismiss = { selectedRecordForShare = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportOptionsModal(
    language: Language,
    onDismiss: () -> Unit,
    onExportText: () -> Unit,
    onExportJson: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF141320),
        scrimColor = Color(0x99000000)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 12.dp)
                .testTag("export_options_modal")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == Language.TH) "เลือกรูปแบบการส่งออก" else "Export Format",
                    color = CyberInk,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CyberMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 1: Text Summary
            ExportOptionCard(
                title = if (language == Language.TH) "สรุปข้อความ (Text Summary)" else "Text Summary",
                description = if (language == Language.TH)
                    "ส่งออกเป็นสรุปอ่านง่าย สำหรับแชร์ในแชทหรือส่งอีเมล"
                else
                    "Human-readable summary ideal for messaging apps or emails.",
                icon = Icons.Default.Description,
                iconColor = NeonBlue,
                onClick = onExportText,
                testTag = "export_option_text"
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Option 2: JSON File Data
            ExportOptionCard(
                title = if (language == Language.TH) "ไฟล์ข้อมูล JSON (JSON Format)" else "JSON Data Format",
                description = if (language == Language.TH)
                    "ส่งออกในรูปแบบ JSON ครบถ้วน สำหรับสำรองข้อมูลหรือนำไปประมวลผลต่อ"
                else
                    "Full raw JSON data for backup, analysis, or data control.",
                icon = Icons.Default.Code,
                iconColor = NeonGreen,
                onClick = onExportJson,
                testTag = "export_option_json"
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(14.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = CyberInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = CyberMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

private fun buildTextSummary(
    records: List<SpeedTestRecord>,
    speedUnit: SpeedUnit,
    language: Language
): String {
    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val sb = StringBuilder()
    sb.append("Zipspeed Test History Report\n")
    sb.append("=========================================\n")
    sb.append("Exported: ${dateFormat.format(Date())}\n")
    sb.append("Total Records: ${records.size}\n\n")

    records.forEachIndexed { i, r ->
        val dlVal = if (speedUnit == SpeedUnit.MB_S) r.downloadMbps / 8.0 else r.downloadMbps
        val ulVal = if (speedUnit == SpeedUnit.MB_S) r.uploadMbps / 8.0 else r.uploadMbps
        sb.append("${i + 1}. ${dateFormat.format(Date(r.timestamp))}\n")
        sb.append("   Server: ${r.serverName}\n")
        sb.append("   Ping: ${r.pingMs} ms | Jitter: ${r.jitterMs} ms\n")
        sb.append("   Download: ${String.format(Locale.US, "%.2f", dlVal)} ${speedUnit.label}\n")
        sb.append("   Upload: ${String.format(Locale.US, "%.2f", ulVal)} ${speedUnit.label}\n")
        sb.append("   Network: ${r.networkType}\n\n")
    }
    return sb.toString()
}

private fun buildJsonExport(records: List<SpeedTestRecord>): String {
    val sb = StringBuilder()
    sb.append("[\n")
    records.forEachIndexed { index, r ->
        sb.append("  {\n")
        sb.append("    \"id\": \"${r.id}\",\n")
        sb.append("    \"timestamp\": ${r.timestamp},\n")
        sb.append("    \"serverName\": \"${r.serverName.replace("\"", "\\\"")}\",\n")
        sb.append("    \"pingMs\": ${r.pingMs},\n")
        sb.append("    \"jitterMs\": ${r.jitterMs},\n")
        sb.append("    \"downloadMbps\": ${r.downloadMbps},\n")
        sb.append("    \"uploadMbps\": ${r.uploadMbps},\n")
        sb.append("    \"networkType\": \"${r.networkType.replace("\"", "\\\"")}\"\n")
        sb.append("  }")
        if (index < records.size - 1) sb.append(",")
        sb.append("\n")
    }
    sb.append("]")
    return sb.toString()
}

@Composable
private fun HistoryCardItem(
    record: SpeedTestRecord,
    speedUnit: SpeedUnit,
    dateFormat: SimpleDateFormat,
    language: Language,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    val dateStr = dateFormat.format(Date(record.timestamp))

    val dlVal = if (speedUnit == SpeedUnit.MB_S) record.downloadMbps / 8.0 else record.downloadMbps
    val ulVal = if (speedUnit == SpeedUnit.MB_S) record.uploadMbps / 8.0 else record.uploadMbps

    val dlStr = String.format(Locale.US, "%.1f", dlVal)
    val ulStr = String.format(Locale.US, "%.1f", ulVal)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CyberPanel)
            .border(1.dp, Color(0x17FFFFFF), RoundedCornerShape(18.dp))
            .clickable { onShare() }
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1.2f)) {
            Text(
                text = dateStr,
                color = CyberInk,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "${record.serverName} • ${record.pingMs}ms",
                color = CyberMuted,
                fontSize = 12.sp
            )
            Text(
                text = record.networkType,
                color = Color(0xFFA9C6FF),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(horizontalAlignment = Alignment.End) {
                // Download
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = "Download",
                        tint = NeonGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$dlStr ${speedUnit.label}",
                        color = NeonGreen,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                // Upload
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = "Upload",
                        tint = NeonPurple,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "$ulStr ${speedUnit.label}",
                        color = NeonPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            IconButton(
                onClick = onShare,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("share_history_item")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share Record Details",
                    tint = NeonBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("delete_history_item")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CyberMuted.copy(alpha = 0.6f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
