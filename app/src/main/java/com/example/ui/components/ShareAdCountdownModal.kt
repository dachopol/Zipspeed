package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Language
import com.example.ui.theme.LocalAppTheme

/**
 * Safe fallback for the share-interstitial surface.
 * It never pretends that an AdMob impression occurred.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareAdCountdownModal(
    language: Language,
    onAdCompletedOrSkipped: () -> Unit,
    onOpenVipModal: () -> Unit,
    onDismiss: () -> Unit
) {
    val theme = LocalAppTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isTh = language == Language.TH

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = theme.colors.cardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("share_ad_fallback_modal"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isTh) "โฆษณาก่อนแชร์ยังไม่ได้เชื่อม AdMob จริง" else "Share interstitial is not configured",
                color = theme.colors.textMain,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isTh)
                    "จึงไม่แสดงโฆษณาจำลองและไม่บังคับนับถอยหลัง ผู้ใช้สามารถแชร์ผลต่อได้"
                else
                    "No fake ad or countdown is shown. Sharing remains available.",
                color = theme.colors.textMuted
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenVipModal,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isTh) "Ad-Free" else "Ad-Free")
                }
                Button(
                    onClick = onAdCompletedOrSkipped,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = theme.colors.primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(if (isTh) "แชร์ต่อ" else "Continue")
                }
            }
        }
    }
}
