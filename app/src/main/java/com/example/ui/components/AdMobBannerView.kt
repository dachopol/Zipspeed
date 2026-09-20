package com.example.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Production ad slot.
 *
 * It stays empty until a real Google Mobile Ads configuration (App ID, ad-unit ID,
 * consent flow and load callbacks) is supplied. This prevents fake ads or UI that
 * pretends an ad was served.
 */
@Composable
fun AdMobBannerView(
    isVipAdFree: Boolean,
    onRemoveAdsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intentionally no-op until real AdMob credentials/configuration are available.
}
