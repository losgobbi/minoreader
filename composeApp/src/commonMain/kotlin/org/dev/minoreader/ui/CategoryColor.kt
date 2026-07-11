package org.dev.minoreader.ui

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/** Deterministic per-category color (same name → same color), readable in light and dark. */
fun categoryColor(category: String): Color {
    if (category.isBlank()) return Color(0xFF9E9E9E)
    var hash = 0
    for (c in category) hash = c.code + (hash shl 5) - hash
    val hue = (abs(hash) % 360).toFloat()
    return Color.hsl(hue, 0.55f, 0.55f)
}
