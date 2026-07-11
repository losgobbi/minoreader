package org.dev.minoreader.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.dev.minoreader.data.ArticleListItem

/**
 * A single article row: one-line title + subtitle (source · colored category),
 * with a category-colored dot on the left. Trailing actions are configurable.
 */
@Composable
fun ArticleRow(
    item: ArticleListItem,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    trailingIcon: ImageVector? = null,
    trailingDesc: String? = null,
    onTrailing: (() -> Unit)? = null,
) {
    val catColor = categoryColor(item.category)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier.size(8.dp).clip(CircleShape).background(catColor),
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                        append(item.feedTitle)
                        append("  ·  ")
                    }
                    withStyle(SpanStyle(color = catColor, fontWeight = FontWeight.Medium)) {
                        append(item.category)
                    }
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        IconButton(onClick = onToggleFavorite) {
            Icon(
                imageVector = if (item.isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = if (item.isFavorite) "Unfavorite" else "Favorite",
            )
        }
        if (trailingIcon != null && onTrailing != null) {
            IconButton(onClick = onTrailing) {
                Icon(trailingIcon, contentDescription = trailingDesc)
            }
        }
    }
}
