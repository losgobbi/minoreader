package org.dev.minoreader.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val SplashBackground = Color(0xFF2C7A7B)

/** How long the RSS mark takes to assemble (dot → inner wave → outer wave). */
private const val RSS_ANIM_MILLIS = 1100

/** Splash screen: the cartoon-newspaper logo with an animated RSS mark (cross-platform). */
@Composable
fun SplashScreen() {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, tween(durationMillis = RSS_ANIM_MILLIS, easing = LinearOutSlowInEasing))
    }

    Box(
        modifier = Modifier.fillMaxSize().background(SplashBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            NewspaperWithRss(newspaperSize = 132.dp, progress = progress.value)
            Spacer(Modifier.height(20.dp))
            Text(
                text = "minoreader",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "your RSS reader",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
            )
        }
    }
}

/** The newspaper with a white RSS mark floating just above its top-right corner. */
@Composable
private fun NewspaperWithRss(newspaperSize: Dp, progress: Float) {
    Box(modifier = Modifier.size(width = newspaperSize * 1.26f, height = newspaperSize * 1.30f)) {
        NewspaperLogo(
            size = newspaperSize,
            modifier = Modifier.align(Alignment.BottomStart),
        )
        RssMark(
            progress = progress,
            modifier = Modifier
                .size(newspaperSize * 0.46f)
                .align(Alignment.TopEnd),
        )
    }
}

/**
 * The RSS "broadcast" mark: a dot with two quarter-circle waves emanating up-right.
 * [progress] 0→1 assembles it: the dot fades/pops in, then each wave sweeps into place.
 */
@Composable
private fun RssMark(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val origin = Offset(s * 0.30f, s * 0.72f) // dot center (bottom-left of the mark)
        val stroke = s * 0.11f

        // dot: pops in over the first third
        val dot = window(progress, 0f, 0.30f)
        if (dot > 0f) {
            drawCircle(
                color = Color.White.copy(alpha = dot),
                radius = s * 0.085f * (0.7f + 0.3f * dot),
                center = origin,
            )
        }
        // inner then outer wave sweep in
        drawWave(origin, radius = s * 0.30f, stroke = stroke, frac = window(progress, 0.28f, 0.62f))
        drawWave(origin, radius = s * 0.52f, stroke = stroke, frac = window(progress, 0.58f, 0.96f))
    }
}

/** A quarter-circle wave centered on [origin], swept from east up to north by [frac]. */
private fun DrawScope.drawWave(origin: Offset, radius: Float, stroke: Float, frac: Float) {
    if (frac <= 0f) return
    drawArc(
        color = Color.White.copy(alpha = frac),
        startAngle = 0f,            // 3 o'clock, just to the right of the dot
        sweepAngle = -90f * frac,   // grows counter-clockwise up to 12 o'clock
        useCenter = false,
        topLeft = Offset(origin.x - radius, origin.y - radius),
        size = Size(radius * 2f, radius * 2f),
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )
}

/** Normalizes [p] to 0→1 across the [a, b] window (clamped). */
private fun window(p: Float, a: Float, b: Float): Float = ((p - a) / (b - a)).coerceIn(0f, 1f)
