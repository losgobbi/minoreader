package org.dev.minoreader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

private val Outline = Color(0xFF263238)
private val Paper = Color(0xFFFFFFFF)
private val Accent = Color(0xFFE4572E)
private val LineGray = Color(0xFF9AA0A6)
private val BackPage = Color(0xFFE7DDC6)

/** Logo: a cartoon newspaper drawn with Compose primitives (no external asset). */
@Composable
fun NewspaperLogo(size: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size)) {
        val corner = RoundedCornerShape(size * 0.07f)
        // back page → folded-newspaper depth
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .align(Alignment.BottomEnd)
                .clip(corner)
                .background(BackPage),
        )
        // newspaper (front)
        Column(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .align(Alignment.TopStart)
                .background(Paper, corner)
                .border(size * 0.03f, Outline, corner)
                .padding(size * 0.1f),
            verticalArrangement = Arrangement.spacedBy(size * 0.035f),
        ) {
            // masthead (newspaper name)
            Box(Modifier.fillMaxWidth().height(size * 0.13f).background(Outline))
            // rule line
            Box(Modifier.fillMaxWidth().height(size * 0.018f).background(Outline))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(size * 0.05f),
            ) {
                // article photo/image
                Box(
                    Modifier
                        .size(size * 0.24f)
                        .background(Accent)
                        .border(size * 0.018f, Outline),
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = size * 0.01f),
                    verticalArrangement = Arrangement.spacedBy(size * 0.03f),
                ) {
                    TextLine(size)
                    TextLine(size)
                    TextLine(size)
                }
            }
            TextLine(size)
            TextLine(size)
            TextLine(size, widthFraction = 0.6f)
        }
    }
}

@Composable
private fun TextLine(size: Dp, widthFraction: Float = 1f) {
    Box(Modifier.fillMaxWidth(widthFraction).height(size * 0.025f).background(LineGray))
}
