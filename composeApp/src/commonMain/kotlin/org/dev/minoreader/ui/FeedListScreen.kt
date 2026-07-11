package org.dev.minoreader.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
            CategoryFilterBar(
                categories = categories,
                selected = selected,
                onSelect = { vm.selectCategory(it) },
            )
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

/**
 * Compact category filter: a single chip showing the current selection that opens a
 * bottom action sheet listing the category names. Keeps the feed header clean instead
 * of a horizontally growing row of chips.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterBar(
    categories: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    var showSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssistChip(
            onClick = { showSheet = true },
            label = { Text(selected ?: "All categories") },
            trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
        )
    }

    if (showSheet) {
        ModalBottomSheet(onDismissRequest = { showSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
                CategorySheetItem(label = "All categories", isSelected = selected == null) {
                    onSelect(null); showSheet = false
                }
                categories.forEach { category ->
                    CategorySheetItem(label = category, isSelected = selected == category) {
                        onSelect(category); showSheet = false
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySheetItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
            )
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
