package no.nordicsemi.memfault.util

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.placeholder(
    visible: Boolean,
    color: Color = Color.Black.copy(alpha = 0.1f),
    highlightColor: Color = Color.White.copy(alpha = 0.3f),
    shape: Shape = RoundedCornerShape(4.dp),
): Modifier {
    if (!visible) return this

    return this
        .clip(shape)
        .shimmerPlaceholder(color, highlightColor)
}

private fun Modifier.shimmerPlaceholder(
    baseColor: Color,
    highlightColor: Color
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerTranslateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing)
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        baseColor,
        highlightColor,
        baseColor,
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = shimmerTranslateAnim, y = shimmerTranslateAnim)
    )

    drawWithContent {
        drawRect(brush)
    }
}