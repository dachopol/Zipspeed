package com.example.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.NeonBlue

/**
 * Adds a visible, high-performance vertical scrollbar on the right edge of a scrollable Column or Box.
 */
fun Modifier.verticalScrollbar(
    scrollState: ScrollState,
    width: Dp = 5.dp,
    thumbColor: Color = NeonBlue.copy(alpha = 0.85f),
    trackColor: Color = Color(0x22FFFFFF),
    cornerRadius: Dp = 3.dp,
    paddingEnd: Dp = 3.dp,
    paddingVertical: Dp = 8.dp,
    minThumbHeight: Dp = 36.dp
): Modifier = this.drawWithContent {
    drawContent()

    val totalRange = scrollState.maxValue + size.height
    if (scrollState.maxValue > 0 && totalRange > 0f) {
        val viewHeight = (size.height - (paddingVertical.toPx() * 2)).coerceAtLeast(1f)
        val thumbHeight = (size.height / totalRange * viewHeight).coerceIn(minThumbHeight.toPx(), viewHeight)

        val scrollOffset = scrollState.value.toFloat()
        val scrollableDistance = scrollState.maxValue.toFloat()
        val maxThumbTravel = (viewHeight - thumbHeight).coerceAtLeast(0f)
        val thumbY = paddingVertical.toPx() + if (scrollableDistance > 0) (scrollOffset / scrollableDistance) * maxThumbTravel else 0f

        val barWidthPx = width.toPx()
        val barRight = size.width - paddingEnd.toPx()
        val barLeft = barRight - barWidthPx

        // Background Track on the right side
        if (trackColor.alpha > 0f) {
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(barLeft, paddingVertical.toPx()),
                size = Size(barWidthPx, viewHeight),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }

        // Active Thumb Indicator
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(barLeft, thumbY),
            size = Size(barWidthPx, thumbHeight),
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )
    }
}

/**
 * Adds a visible vertical scrollbar on the right edge for LazyColumn using LazyListState.
 */
fun Modifier.verticalScrollbar(
    lazyListState: LazyListState,
    width: Dp = 5.dp,
    thumbColor: Color = NeonBlue.copy(alpha = 0.85f),
    trackColor: Color = Color(0x22FFFFFF),
    cornerRadius: Dp = 3.dp,
    paddingEnd: Dp = 3.dp,
    paddingVertical: Dp = 8.dp,
    minThumbHeight: Dp = 36.dp
): Modifier = this.drawWithContent {
    drawContent()

    val layoutInfo = lazyListState.layoutInfo
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo

    if (totalItems > 0 && visibleItems.isNotEmpty() && totalItems > visibleItems.size) {
        val viewHeight = (size.height - (paddingVertical.toPx() * 2)).coerceAtLeast(1f)
        val thumbHeight = ((visibleItems.size.toFloat() / totalItems.toFloat()) * viewHeight).coerceIn(minThumbHeight.toPx(), viewHeight)

        val firstVisibleIndex = lazyListState.firstVisibleItemIndex.toFloat()
        val firstVisibleOffset = lazyListState.firstVisibleItemScrollOffset.toFloat()
        val estimatedItemHeight = visibleItems.firstOrNull()?.size?.toFloat() ?: 100f
        val currentScrollProgress = (firstVisibleIndex + (firstVisibleOffset / estimatedItemHeight.coerceAtLeast(1f))) / totalItems.toFloat()

        val maxThumbTravel = (viewHeight - thumbHeight).coerceAtLeast(0f)
        val thumbY = (paddingVertical.toPx() + (currentScrollProgress * maxThumbTravel)).coerceIn(
            paddingVertical.toPx(),
            paddingVertical.toPx() + maxThumbTravel
        )

        val barWidthPx = width.toPx()
        val barRight = size.width - paddingEnd.toPx()
        val barLeft = barRight - barWidthPx

        // Track
        if (trackColor.alpha > 0f) {
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(barLeft, paddingVertical.toPx()),
                size = Size(barWidthPx, viewHeight),
                cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
            )
        }

        // Thumb
        drawRoundRect(
            color = thumbColor,
            topLeft = Offset(barLeft, thumbY),
            size = Size(barWidthPx, thumbHeight),
            cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx())
        )
    }
}
