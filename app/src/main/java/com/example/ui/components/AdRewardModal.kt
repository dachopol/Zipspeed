package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
 * Safe fallback for the rewarded-ad surface.
 * No reward is granted locally. A future real AdMob rewarded callback must invoke
 * the entitlement flow only after the SDK reports a completed reward event.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdRewardModal(
    language: Language,
    onDismiss: () -> Unit,
    onClaimReward: () -> Unit
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
                .testTag("ad_reward_modal"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isTh) "Rewarded ad ยังไม่พร้อมใช้งาน" else "Rewarded ad is not configured yet",
                color = theme.colors.textMain,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isTh)
                    "แอปจะไม่สร้างโฆษณาจำลองหรือปลดสิทธิ์เอง ต้องรอ AdMob จริงโหลดสำเร็จและยืนยัน reward callback ก่อน"
                else
                    "Zipspeed does not simulate ads or grant rewards locally. A real AdMob load and reward callback is required.",
                color = theme.colors.textMuted
            )
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = theme.colors.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isTh) "ปิด" else "Close")
            }
        }
    }
}
