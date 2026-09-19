package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The dashboard ad is shown inside [SpeedGauge] after a completed test.
 * Keeping this component as a no-op preserves the existing call site while
 * preventing a second banner/action from competing with the gauge controls.
 */
@Composable
fun AdMobBannerView(
    isVipAdFree: Boolean,
    onRemoveAdsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intentionally empty: the gauge owns the single dashboard ad surface.
}
