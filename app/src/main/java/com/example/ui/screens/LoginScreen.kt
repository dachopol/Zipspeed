package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Language
import com.example.model.UserProfile
import com.example.ui.theme.CyberInk
import com.example.ui.theme.CyberMuted
import com.example.ui.theme.NeonBlue

@Composable
fun LoginScreen(
    language: Language,
    onLoginSuccess: (UserProfile) -> Unit,
    onSkipGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Top Icon Logo
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(1.5.dp, Color(0x552F7BFF), RoundedCornerShape(22.dp)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_zipspeed_logo_1789164151247),
                contentDescription = "Zipspeed Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(22.dp))
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.str_welcome_to_zipspeed_112),
            color = CyberInk,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.str_sync_test_history_and_unlock_s_113),
            color = CyberMuted,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Google Login
        SocialLoginButton(
            text = stringResource(R.string.str_continue_with_google_114),
            iconColor = Color(0xFF4285F4),
            iconChar = "G",
            testTag = "login_google_button",
            onClick = {
                onLoginSuccess(
                    UserProfile(
                        name = "Google User",
                        email = "user@gmail.com",
                        provider = "Google"
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Facebook Login
        SocialLoginButton(
            text = stringResource(R.string.str_continue_with_facebook_115),
            iconColor = Color(0xFF1877F2),
            iconChar = "f",
            testTag = "login_facebook_button",
            onClick = {
                onLoginSuccess(
                    UserProfile(
                        name = "Facebook User",
                        email = "user@facebook.com",
                        provider = "Facebook"
                    )
                )
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.str_or_try_out_the_app_without_log_116),
            color = CyberMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Skip / Guest
        TextButton(
            onClick = onSkipGuest,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("login_skip_button")
        ) {
            Text(
                text = stringResource(R.string.str_skip_continue_as_guest_117),
                color = Color(0xFFA9C6FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SocialLoginButton(
    text: String,
    iconColor: Color,
    iconChar: String,
    testTag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0x1AFFFFFF))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = iconChar,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = text,
            color = CyberInk,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
