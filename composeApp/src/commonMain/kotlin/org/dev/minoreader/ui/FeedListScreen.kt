package org.dev.minoreader.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

enum class ListMode { FEED, READ, FAVORITES }

@Composable
fun FeedListScreen(
    vm: MainViewModel,
    mode: ListMode,
    onOpenArticle: (Long) -> Unit,
) {
    val flow = when (mode) {
        ListMode.FEED -> vm.feed
        ListMode.READ -> vm.read
        ListMode.FAVORITES -> vm.favorites
    }
    val items by flow.collectAsState()
    val categories by vm.categories.collectAsState()
    val selected by vm.selectedCategory.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (mode == ListMode.FEED && categories.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = selected == null,
                        onClick = { vm.selectCategory(null) },
                        label = { Text("All") },
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selected == category,
                        onClick = { vm.selectCategory(category) },
                        label = { Text(category) },
                    )
                }
            }
        }

        if (items.isEmpty()) {
            EmptyState(mode)
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(items, key = { it.id }) { item ->
                    val trailingIcon: ImageVector? = when (mode) {
                        ListMode.FEED -> Icons.Filled.Check
                        ListMode.READ -> Icons.AutoMirrored.Filled.Undo
                        ListMode.FAVORITES -> null
                    }
                    val trailingDesc: String? = when (mode) {
                        ListMode.FEED -> "Mark as read"
                        ListMode.READ -> "Mark as unread"
                        ListMode.FAVORITES -> null
                    }
                    val onTrailing: (() -> Unit)? = when (mode) {
                        ListMode.FEED -> ({ vm.markRead(item.id, true) })
                        ListMode.READ -> ({ vm.markRead(item.id, false) })
                        ListMode.FAVORITES -> null
                    }
                    ArticleRow(
                        item = item,
                        onOpen = { onOpenArticle(item.id) },
                        onToggleFavorite = { vm.toggleFavorite(item) },
                        trailingIcon = trailingIcon,
                        trailingDesc = trailingDesc,
                        onTrailing = onTrailing,
                    )
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun EmptyState(mode: ListMode) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = when (mode) {
                ListMode.FEED -> "Nothing here yet.\nAdd feeds in Settings and tap refresh."
                ListMode.READ -> "No read articles yet."
                ListMode.FAVORITES -> "No favorites yet."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
