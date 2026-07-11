package org.dev.minoreader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.dev.minoreader.reader.ContentBlock
import org.dev.minoreader.reader.TextSpan

/** Renders the article (list of ContentBlock) as native Compose. */
@Composable
fun ArticleContentView(blocks: List<ContentBlock>, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        blocks.forEach { BlockView(it) }
    }
}

@Composable
private fun BlockView(block: ContentBlock) {
    when (block) {
        is ContentBlock.Heading -> Text(
            text = block.text,
            style = when (block.level) {
                1 -> MaterialTheme.typography.headlineSmall
                2 -> MaterialTheme.typography.titleLarge
                else -> MaterialTheme.typography.titleMedium
            },
            modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
        )

        is ContentBlock.Paragraph -> Text(
            text = spansToAnnotated(block.spans),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(vertical = 6.dp),
        )

        is ContentBlock.Bullet -> Row(modifier = Modifier.padding(vertical = 3.dp)) {
            Text("•  ", style = MaterialTheme.typography.bodyLarge)
            Text(
                text = spansToAnnotated(block.spans),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
        }

        is ContentBlock.Quote -> Row(
            modifier = Modifier.height(IntrinsicSize.Min).padding(vertical = 6.dp),
        ) {
            Box(modifier = Modifier.width(3.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outline))
            Spacer(Modifier.width(10.dp))
            Text(
                text = block.text,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        is ContentBlock.Code -> Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        ) {
            Text(
                text = block.text,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(10.dp),
            )
        }

        is ContentBlock.Image -> {
            val label = block.alt?.let { "🖼  $it" } ?: "🖼  [image]"
            Text(
                text = buildAnnotatedString {
                    withLink(LinkAnnotation.Url(block.url)) { append(label) }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }

        ContentBlock.Divider -> HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
    }
}

@Composable
private fun spansToAnnotated(spans: List<TextSpan>): AnnotatedString {
    val linkColor = MaterialTheme.colorScheme.primary
    return buildAnnotatedString {
        spans.forEach { span ->
            val style = SpanStyle(
                fontWeight = if (span.bold) FontWeight.Bold else null,
                fontStyle = if (span.italic) FontStyle.Italic else null,
            )
            val link = span.link
            if (link != null) {
                withLink(
                    LinkAnnotation.Url(
                        url = link,
                        styles = TextLinkStyles(
                            SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                        ),
                    ),
                ) {
                    withStyle(style) { append(span.text) }
                }
            } else {
                withStyle(style) { append(span.text) }
            }
        }
    }
}
